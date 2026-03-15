package detector

import (
	"bufio"
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"strings"
)

// PluginManager detected on the user's system
type PluginManager string

const (
	LazyNvim  PluginManager = "lazy.nvim"
	Packer    PluginManager = "packer"
	VimPlug   PluginManager = "vim-plug"
	Dein      PluginManager = "dein"
	Manual    PluginManager = "manual"
	Unknown   PluginManager = "unknown"
)

// ConfigType detected
type ConfigType string

const (
	Lua     ConfigType = "lua"    // init.lua
	VimScript ConfigType = "vim"  // init.vim
)

// DetectionResult holds everything found about the user's Neovim setup
type DetectionResult struct {
	ConfigDir     string
	ConfigFile    string
	ConfigType    ConfigType
	PluginManager PluginManager
	PluginFile    string        // exact file where plugins are declared
	PluginLine    int           // line number where new plugins should be inserted
	NvimVersion   string
	InstalledPlugins []string
}

// Detect scans the user's Neovim config and returns a DetectionResult
func Detect() (*DetectionResult, error) {
	configDir, err := getNvimConfigDir()
	if err != nil {
		return nil, fmt.Errorf("could not find Neovim config directory: %w", err)
	}

	result := &DetectionResult{ConfigDir: configDir}

	// Detect config file type
	initLua := filepath.Join(configDir, "init.lua")
	initVim := filepath.Join(configDir, "init.vim")

	if fileExists(initLua) {
		result.ConfigFile = initLua
		result.ConfigType = Lua
	} else if fileExists(initVim) {
		result.ConfigFile = initVim
		result.ConfigType = VimScript
	} else {
		return nil, fmt.Errorf("no init.lua or init.vim found in %s", configDir)
	}

	// Detect plugin manager
	result.PluginManager, result.PluginFile, result.PluginLine = detectPluginManager(configDir, result.ConfigFile)

	// Read installed plugins
	result.InstalledPlugins = scanInstalledPlugins(result.PluginFile, result.PluginManager)

	fmt.Printf("   Config dir:     %s\n", result.ConfigDir)
	fmt.Printf("   Config type:    %s\n", result.ConfigType)
	fmt.Printf("   Plugin manager: %s\n", result.PluginManager)
	fmt.Printf("   Plugins found:  %d\n", len(result.InstalledPlugins))

	return result, nil
}

func getNvimConfigDir() (string, error) {
	var configBase string

	switch runtime.GOOS {
	case "windows":
		configBase = os.Getenv("LOCALAPPDATA")
	default:
		// Linux, macOS, WSL
		xdgConfig := os.Getenv("XDG_CONFIG_HOME")
		if xdgConfig != "" {
			configBase = xdgConfig
		} else {
			home, err := os.UserHomeDir()
			if err != nil {
				return "", err
			}
			configBase = filepath.Join(home, ".config")
		}
	}

	nvimDir := filepath.Join(configBase, "nvim")
	if !fileExists(nvimDir) {
		return "", fmt.Errorf("~/.config/nvim does not exist — is Neovim installed?")
	}
	return nvimDir, nil
}

func detectPluginManager(configDir, configFile string) (PluginManager, string, int) {
	// Check for lazy.nvim — look for require("lazy") or lazy.setup
	if pm, file, line := searchInDir(configDir, []string{
		`require("lazy")`,
		`require('lazy')`,
		`lazy.setup`,
	}); pm {
		return LazyNvim, file, line
	}

	// Check for packer — look for require("packer") or use(
	if pm, file, line := searchInDir(configDir, []string{
		`require("packer")`,
		`require('packer')`,
		`packer.startup`,
	}); pm {
		return Packer, file, line
	}

	// Check for vim-plug — look for call plug#begin
	if pm, file, line := searchInDir(configDir, []string{
		`call plug#begin`,
		`plug#begin`,
	}); pm {
		return VimPlug, file, line
	}

	// Default: user is from scratch
	return Manual, configFile, 0
}

func searchInDir(dir string, patterns []string) (bool, string, int) {
	// Search init.lua / init.vim and lua/ directory recursively
	var found bool
	var foundFile string
	var foundLine int

	filepath.Walk(dir, func(path string, info os.FileInfo, err error) error {
		if err != nil || info.IsDir() {
			return nil
		}
		ext := filepath.Ext(path)
		if ext != ".lua" && ext != ".vim" {
			return nil
		}

		file, err := os.Open(path)
		if err != nil {
			return nil
		}
		defer file.Close()

		scanner := bufio.NewScanner(file)
		lineNum := 0
		for scanner.Scan() {
			lineNum++
			line := scanner.Text()
			for _, pattern := range patterns {
				if strings.Contains(line, pattern) {
					found = true
					foundFile = path
					foundLine = lineNum
					return filepath.SkipAll
				}
			}
		}
		return nil
	})

	return found, foundFile, foundLine
}

func scanInstalledPlugins(pluginFile string, manager PluginManager) []string {
	if pluginFile == "" {
		return nil
	}

	file, err := os.Open(pluginFile)
	if err != nil {
		return nil
	}
	defer file.Close()

	var plugins []string
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())

		switch manager {
		case LazyNvim:
			// Match: "owner/repo" inside lazy spec
			if strings.Contains(line, `"`) && strings.Contains(line, "/") {
				if plugin := extractGitHubSlug(line); plugin != "" {
					plugins = append(plugins, plugin)
				}
			}
		case Packer:
			// Match: use "owner/repo" or use { "owner/repo" }
			if strings.HasPrefix(line, "use") {
				if plugin := extractGitHubSlug(line); plugin != "" {
					plugins = append(plugins, plugin)
				}
			}
		case VimPlug:
			// Match: Plug 'owner/repo'
			if strings.HasPrefix(line, "Plug") {
				if plugin := extractGitHubSlug(line); plugin != "" {
					plugins = append(plugins, plugin)
				}
			}
		}
	}
	return plugins
}

func extractGitHubSlug(line string) string {
	// Extract owner/repo from a line containing quotes
	for _, quote := range []string{`"`, `'`} {
		start := strings.Index(line, quote)
		if start == -1 {
			continue
		}
		end := strings.Index(line[start+1:], quote)
		if end == -1 {
			continue
		}
		slug := line[start+1 : start+1+end]
		if strings.Contains(slug, "/") && !strings.Contains(slug, " ") {
			return slug
		}
	}
	return ""
}

func fileExists(path string) bool {
	_, err := os.Stat(path)
	return !os.IsNotExist(err)
}
