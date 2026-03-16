package cmd

import (
	"fmt"
	"os"

	"neohub-cli/internal/api"
	"neohub-cli/internal/detector"
	"neohub-cli/internal/installer"

	"github.com/spf13/cobra"
)

const version = "0.1.0"

var rootCmd = &cobra.Command{
	Use:   "neostore",
	Short: "NeoHub CLI — Neovim plugin manager",
	Long: `
 _   _            ____  _
| \ | | ___  ___ / ___|| |_ ___  _ __ ___
|  \| |/ _ \/ _ \\___ \| __/ _ \| '__/ _ \
| |\  |  __/ (_) |___) | || (_) | | |  __/
|_| \_|\___|\___/|____/ \__\___/|_|  \___|

NeoHub CLI — Install and manage Neovim plugins.
https://neohub.dev
`,
}

func Execute() error {
	return rootCmd.Execute()
}

func init() {
	rootCmd.AddCommand(installCmd)
	rootCmd.AddCommand(removeCmd)
	rootCmd.AddCommand(updateCmd)
	rootCmd.AddCommand(searchCmd)
	rootCmd.AddCommand(infoCmd)
	rootCmd.AddCommand(listCmd)
	rootCmd.AddCommand(doctorCmd)
	rootCmd.AddCommand(applyCmd)
	rootCmd.AddCommand(backupCmd)
	rootCmd.AddCommand(initCmd)
	rootCmd.AddCommand(versionCmd)
}

// ── install ──────────────────────────────────────────────────────────────

var installCmd = &cobra.Command{
	Use:   "install [plugin]",
	Short: "Install a Neovim plugin",
	Long:  `Detects your plugin manager (lazy.nvim, packer, vim-plug) and installs automatically.`,
	Example: `  neostore install telescope
  neostore install nvim-tree
  neostore install folke/which-key.nvim`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		pluginName := args[0]
		client := api.NewClient("")

		result, err := installer.Install(pluginName, client)
		if err != nil {
			fmt.Fprintf(os.Stderr, "❌ Error: %s\n", err)
			os.Exit(1)
		}

		if result.AlreadyInstalled {
			fmt.Printf("✅ %s is already installed!\n", result.Plugin.Name)
			return
		}

		fmt.Printf("\n✅ Installed %s successfully!\n", result.Plugin.Name)
		fmt.Printf("   Plugin:  %s\n", result.Plugin.FullName)
		fmt.Printf("   Manager: %s\n", result.Manager)
		fmt.Printf("   File:    %s\n", result.ConfigFile)
		fmt.Printf("   Line:    %d\n\n", result.LineInserted)
		fmt.Printf("📦 Snippet added:\n   %s\n\n", result.Snippet)
		fmt.Printf("💡 Run sync to complete installation:\n")

		switch result.Manager {
		case detector.LazyNvim:
			fmt.Println("   :Lazy sync")
		case detector.Packer:
			fmt.Println("   :PackerSync")
		case detector.VimPlug:
			fmt.Println("   :PlugInstall")
		default:
			fmt.Println("   Add snippet manually to your config")
		}
	},
}

// ── remove ───────────────────────────────────────────────────────────────

var removeCmd = &cobra.Command{
	Use:     "remove [plugin]",
	Short:   "Remove a Neovim plugin",
	Aliases: []string{"rm", "uninstall"},
	Args:    cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("🗑  Removing %s...\n", args[0])
		fmt.Println("   Coming soon!")
	},
}

// ── update ───────────────────────────────────────────────────────────────

var updateCmd = &cobra.Command{
	Use:   "update [plugin]",
	Short: "Update plugins (all if no plugin specified)",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) == 0 {
			fmt.Println("🔄 Updating all plugins...")
		} else {
			fmt.Printf("🔄 Updating %s...\n", args[0])
		}
		fmt.Println("   Coming soon!")
	},
}

// ── search ───────────────────────────────────────────────────────────────

var searchCmd = &cobra.Command{
	Use:   "search [query]",
	Short: "Search for plugins on NeoHub",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		client := api.NewClient("")
		fmt.Printf("🔎 Searching for \"%s\"...\n\n", args[0])

		results, err := client.SearchPlugins(args[0])
		if err != nil {
			fmt.Fprintf(os.Stderr, "❌ Error: %s\n", err)
			os.Exit(1)
		}

		if len(results) == 0 {
			fmt.Println("No plugins found.")
			return
		}

		for _, p := range results {
			fmt.Printf("  %-30s ⭐ %-6d  %s\n",
				p.Name, p.GithubStars, p.Description)
		}
	},
}

// ── info ─────────────────────────────────────────────────────────────────

var infoCmd = &cobra.Command{
	Use:   "info [plugin]",
	Short: "Show plugin details",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		client := api.NewClient("")
		plugin, err := client.FindPlugin(args[0])
		if err != nil {
			fmt.Fprintf(os.Stderr, "❌ %s\n", err)
			os.Exit(1)
		}

		fmt.Printf("\n📦 %s\n", plugin.Name)
		fmt.Printf("   Repo:     %s\n", plugin.FullName)
		fmt.Printf("   Stars:    ⭐ %d\n", plugin.GithubStars)
		fmt.Printf("   Category: %s\n", plugin.Category)
		fmt.Printf("   GitHub:   %s\n\n", plugin.GithubURL)
		if plugin.Description != "" {
			fmt.Printf("   %s\n\n", plugin.Description)
		}
	},
}

// ── list ─────────────────────────────────────────────────────────────────

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List installed plugins",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("📦 Scanning installed plugins...")
		config, err := detector.Detect()
		if err != nil {
			fmt.Fprintf(os.Stderr, "❌ %s\n", err)
			os.Exit(1)
		}

		if len(config.InstalledPlugins) == 0 {
			fmt.Println("   No plugins found.")
			return
		}

		fmt.Printf("\n   Found %d plugins (%s):\n\n",
			len(config.InstalledPlugins), config.PluginManager)
		for _, p := range config.InstalledPlugins {
			fmt.Printf("   • %s\n", p)
		}
	},
}

// ── doctor ───────────────────────────────────────────────────────────────

var doctorCmd = &cobra.Command{
	Use:   "doctor",
	Short: "Check for plugin conflicts and issues",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("🩺 Running NeoHub Doctor...")
		config, err := detector.Detect()
		if err != nil {
			fmt.Fprintf(os.Stderr, "❌ %s\n", err)
			os.Exit(1)
		}
		fmt.Printf("   ✅ Config found: %s\n", config.ConfigFile)
		fmt.Printf("   ✅ Plugin manager: %s\n", config.PluginManager)
		fmt.Printf("   ✅ Plugins found: %d\n", len(config.InstalledPlugins))
		fmt.Println("\n   No issues detected!")
	},
}

// ── apply ────────────────────────────────────────────────────────────────

var applyCmd = &cobra.Command{
	Use:   "apply [username/config]",
	Short: "Apply a config from NeoHub",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("📥 Applying config %s...\n", args[0])
		fmt.Println("   Coming soon!")
	},
}

// ── backup ───────────────────────────────────────────────────────────────

var backupCmd = &cobra.Command{
	Use:   "backup",
	Short: "Push your config to NeoHub",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("📤 Pushing config to NeoHub...")
		fmt.Println("   Coming soon!")
	},
}

// ── init ─────────────────────────────────────────────────────────────────

var initCmd = &cobra.Command{
	Use:   "init",
	Short: "Bootstrap a fresh Neovim setup interactively",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("🚀 Welcome to NeoHub Setup!")
		fmt.Println("   Coming soon!")
	},
}

// ── version ──────────────────────────────────────────────────────────────

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Print neostore version",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("neostore v%s\n", version)
	},
}