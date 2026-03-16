import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { DomSanitizer, SafeHtml } from "@angular/platform-browser";
import { marked } from "marked";
import {
  PluginDetail,
  PluginService,
} from "../../../../core/services/plugin.service";

@Component({
  selector: "app-plugin-detail",
  templateUrl: "./plugin-detail.component.html",
  styleUrls: ["./plugin-detail.component.scss"],
})
export class PluginDetailComponent implements OnInit {
  plugin: PluginDetail | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pluginService: PluginService,
    private sanitizer: DomSanitizer,
  ) {}

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get("slug");
    if (!slug) {
      this.router.navigate(["/"]);
      return;
    }

    this.pluginService.getPlugin(slug).subscribe({
      next: (plugin) => {
        this.plugin = plugin;
        this.loading = false;
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
  }

  getInstallSnippet(manager: "lazy" | "packer" | "plug"): string {
    if (!this.plugin) return "";
    const repo = this.plugin.fullName;
    switch (manager) {
      case "lazy":
        return `{ '${repo}' }`;
      case "packer":
        return `use '${repo}'`;
      case "plug":
        return `Plug '${repo}'`;
    }
  }
}
