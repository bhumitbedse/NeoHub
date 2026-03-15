package cmd

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

const apiBase = "http://localhost:8080/api/v1"
const version = "0.1.0"

var rootCmd = &cobra.Command{
	Use:   "neostore",
	Short: "NeoHub CLI — Neovim plugin manager",
	Long: `
 _   _            _   _       _     
| \ | | ___  ___ | | | |_   _| |__  
|  \| |/ _ \/ _ \| |_| | | | | '_ \ 
| |\  |  __/ (_) |  _  | |_| | |_) |
|_| \_|\___|\___/|_| |_|\__,_|_.__/ 
                                     
NeoHub CLI — Install and manage Neovim plugins.
https://neohub.dev
`,
}

// Execute runs the root command
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

// ── install ──────────────────────────────────────

var installCmd = &cobra.Command{
	Use:   "install [plugin]",
	Short: "Install a Neovim plugin",
	Long:  `Detects your plugin manager (lazy.nvim, packer, vim-plug) and installs the plugin automatically.`,
	Example: `  neostore install telescope
  neostore install telescope@0.1.5
  neostore install nvim-tree`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("🔍 Detecting Neovim config...\n")
		// TODO: call internal/detector
		// TODO: call internal/installer
		fmt.Printf("✅ Installed %s\n", args[0])
	},
}

// ── remove ───────────────────────────────────────

var removeCmd = &cobra.Command{
	Use:     "remove [plugin]",
	Short:   "Remove a Neovim plugin",
	Aliases: []string{"rm", "uninstall"},
	Args:    cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("🗑  Removing %s...\n", args[0])
		// TODO: implement removal
	},
}

// ── update ───────────────────────────────────────

var updateCmd = &cobra.Command{
	Use:   "update [plugin]",
	Short: "Update plugins (all if no plugin specified)",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) == 0 {
			fmt.Println("🔄 Updating all plugins...")
		} else {
			fmt.Printf("🔄 Updating %s...\n", args[0])
		}
		// TODO: implement update
	},
}

// ── search ───────────────────────────────────────

var searchCmd = &cobra.Command{
	Use:   "search [query]",
	Short: "Search for plugins on NeoHub",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("🔎 Searching for \"%s\"...\n", args[0])
		// TODO: call NeoHub API search endpoint
	},
}

// ── info ─────────────────────────────────────────

var infoCmd = &cobra.Command{
	Use:   "info [plugin]",
	Short: "Show plugin details and keymaps",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("ℹ  Fetching info for %s...\n", args[0])
		// TODO: call NeoHub API
	},
}

// ── list ─────────────────────────────────────────

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List installed plugins",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("📦 Installed plugins:")
		// TODO: read from local config
	},
}

// ── doctor ───────────────────────────────────────

var doctorCmd = &cobra.Command{
	Use:   "doctor",
	Short: "Check for plugin conflicts and broken configs",
	Long:  `Scans your Neovim config for conflicts, missing dependencies, and deprecated APIs.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("🩺 Running NeoHub Doctor...")
		fmt.Println("   Scanning ~/.config/nvim/...")
		// TODO: implement conflict detection
	},
}

// ── apply ────────────────────────────────────────

var applyCmd = &cobra.Command{
	Use:   "apply [username/config]",
	Short: "Apply a config from NeoHub",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("📥 Applying config %s...\n", args[0])
		// TODO: fetch and apply config
	},
}

// ── backup ───────────────────────────────────────

var backupCmd = &cobra.Command{
	Use:   "backup",
	Short: "Push your config to NeoHub",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("📤 Pushing config to NeoHub...")
		// TODO: implement backup
	},
}

// ── init ─────────────────────────────────────────

var initCmd = &cobra.Command{
	Use:   "init",
	Short: "Bootstrap a fresh Neovim setup interactively",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("🚀 Welcome to NeoHub Setup!")
		fmt.Println("   Let's get your Neovim configured...")
		// TODO: interactive setup wizard
	},
}

// ── version ──────────────────────────────────────

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Print neostore version",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("neostore v%s\n", version)
		os.Exit(0)
	},
}
