package installer

import (
	"bufio"
	"fmt"
	"os"
	"strings"

	"neohub-cli/internal/api"
	"neohub-cli/internal/detector"
)

// Result holds what happened during install
type Result struct {
	Plugin      *api.PluginDetail
	Manager     detector.PluginManager
	Snippet     string
	ConfigFile  string
	LineInserted int
	AlreadyInstalled bool
}

// Install installs a plugin into the user's Neovim config
func Install(pluginName string, client *api.Client) (*Result, error) {

	// 1. Detect Neovim config
	fmt.Println("🔍 Detecting Neovim configuration...")
	config, err := detector.Detect()
	if err != nil {
		return nil, fmt.Errorf("could not detect Neovim config: %w", err)
	}

	// 2. Find plugin on NeoHub
	fmt.Printf("🌐 Looking up '%s' on NeoHub...\n", pluginName)
	plugin, err := client.FindPlugin(pluginName)
	if err != nil {
		return nil, err
	}
	fmt.Printf("   Found: %s (%s) ⭐ %d\n",
		plugin.Name, plugin.FullName, plugin.GithubStars)

	// 3. Check if already installed
	for _, installed := range config.InstalledPlugins {
		if strings.EqualFold(installed, plugin.FullName) ||
			strings.Contains(installed, plugin.GithubRepo) {
			return &Result{
				Plugin:           plugin,
				Manager:          config.PluginManager,
				AlreadyInstalled: true,
			}, nil
		}
	}

	// 4. Generate install snippet
	snippet := generateSnippet(plugin.FullName, config.PluginManager)

	// 5. Inject into config file
	line, err := injectSnippet(config, snippet, plugin.FullName)
	if err != nil {
		return nil, fmt.Errorf("failed to inject snippet: %w", err)
	}

	return &Result{
		Plugin:       plugin,
		Manager:      config.PluginManager,
		Snippet:      snippet,
		ConfigFile:   config.PluginFile,
		LineInserted: line,
	}, nil
}

// generateSnippet returns the correct install snippet for the plugin manager
func generateSnippet(fullName string, manager detector.PluginManager) string {
	switch manager {
	case detector.LazyNvim:
		return fmt.Sprintf(`  { "%s" },`, fullName)
	case detector.Packer:
		return fmt.Sprintf(`  use "%s"`, fullName)
	case detector.VimPlug:
		return fmt.Sprintf(`Plug '%s'`, fullName)
	default:
		return fmt.Sprintf(`-- Add manually: require("%s")`, fullName)
	}
}

// injectSnippet writes the snippet into the config file at the right location
func injectSnippet(config *detector.DetectionResult,
	snippet, fullName string) (int, error) {

	file, err := os.Open(config.PluginFile)
	if err != nil {
		return 0, err
	}

	var lines []string
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		lines = append(lines, scanner.Text())
	}
	file.Close()

	insertAt := findInsertionPoint(lines, config.PluginManager, config.PluginLine)
	if insertAt < 0 {
		return 0, fmt.Errorf("could not find insertion point in %s",
			config.PluginFile)
	}

	// Insert snippet
	newLines := make([]string, 0, len(lines)+2)
	newLines = append(newLines, lines[:insertAt]...)
	newLines = append(newLines, snippet)
	newLines = append(newLines, lines[insertAt:]...)

	// Write back
	out, err := os.Create(config.PluginFile)
	if err != nil {
		return 0, err
	}
	defer out.Close()

	writer := bufio.NewWriter(out)
	for _, line := range newLines {
		fmt.Fprintln(writer, line)
	}
	return insertAt + 1, writer.Flush()
}

// findInsertionPoint finds where to insert the plugin snippet
func findInsertionPoint(lines []string,
	manager detector.PluginManager, pluginLine int) int {

	switch manager {
	case detector.LazyNvim:
		// Find the lazy.setup({ block and insert before the closing })
		inSetup := false
		for i, line := range lines {
			trimmed := strings.TrimSpace(line)
			if strings.Contains(trimmed, "lazy.setup") ||
				strings.Contains(trimmed, "require(\"lazy\").setup") ||
				strings.Contains(trimmed, "require('lazy').setup") {
				inSetup = true
			}
			if inSetup && (trimmed == "})" || trimmed == "})") {
				return i
			}
		}

	case detector.Packer:
		// Find packer.startup(function(use) block end
		for i, line := range lines {
			trimmed := strings.TrimSpace(line)
			if strings.Contains(trimmed, "packer.startup") {
				// Insert before end of startup block
				for j := i + 1; j < len(lines); j++ {
					t := strings.TrimSpace(lines[j])
					if t == "end)" || t == "end))" {
						return j
					}
				}
			}
		}

	case detector.VimPlug:
		// Find call plug#end() and insert before it
		for i, line := range lines {
			if strings.Contains(strings.ToLower(line), "plug#end") {
				return i
			}
		}
	}

	// Fallback: insert after the detected plugin line
	if pluginLine > 0 {
		return pluginLine
	}
	return len(lines) - 1
}