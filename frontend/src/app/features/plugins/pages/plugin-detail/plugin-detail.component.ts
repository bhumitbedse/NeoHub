import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { DomSanitizer, SafeHtml } from "@angular/platform-browser";
import { marked } from "marked";
import { MatSnackBar } from "@angular/material/snack-bar";
import { PluginDetail, PluginService } from "../../../../core/services/plugin.service";

declare var Prism: any;

@Component({
  selector: "app-plugin-detail",
  templateUrl: "./plugin-detail.component.html",
  styleUrls: ["./plugin-detail.component.scss"],
})
export class PluginDetailComponent implements OnInit {
  plugin: PluginDetail | null = null;
  loading = true;
  isFavorite = false;
  cliCopied = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pluginService: PluginService,
    private sanitizer: DomSanitizer,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get("slug");
    if (!slug) { this.router.navigate(["/"]); return; }

    this.pluginService.getPlugin(slug).subscribe({
      next: (plugin) => { 
        this.plugin = plugin; 
        this.loading = false; 
        setTimeout(() => { if (typeof Prism !== 'undefined') Prism.highlightAll(); }, 100);
      },
      error: () => this.router.navigate(["/"]),
    });
  }

  renderMarkdown(content: string): SafeHtml {
    if (!content) return "";
    const html = marked.parse(content) as string;
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  formatStars(stars: number): string {
    if (!stars) return "0";
    if (stars >= 1000) return (stars / 1000).toFixed(1) + "k";
    return stars.toString();
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text);
    this.snackBar.open("Copied to clipboard!", "Close", { duration: 3000, horizontalPosition: "right", verticalPosition: "bottom", panelClass: ["neo-toast"] });
  }

  copyNeoHubCLI(): void {
    if (!this.plugin) return;
    navigator.clipboard.writeText(`neohub install ${this.plugin.slug}`);
    this.cliCopied = true;
    this.snackBar.open(`Copied NeoHub CLI command!`, "Close", { duration: 3000, horizontalPosition: "right", verticalPosition: "bottom", panelClass: ["neo-toast"] });
    setTimeout(() => this.cliCopied = false, 2000);
  }

  getInstallSnippet(manager: "lazy" | "packer" | "plug"): string {
    if (!this.plugin) return "";
    const repo = this.plugin.fullName;
    switch (manager) {
      case "lazy":   return `{ '${repo}' }`;
      case "packer": return `use '${repo}'`;
      case "plug":   return `Plug '${repo}'`;
    }
  }
}
