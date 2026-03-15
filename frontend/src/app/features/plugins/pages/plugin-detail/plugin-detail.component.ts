import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PluginDetail, PluginService } from '../../../../core/services/plugin.service';

@Component({
  selector: 'app-plugin-detail',
  templateUrl: './plugin-detail.component.html',
  styleUrls: ['./plugin-detail.component.scss']
})
export class PluginDetailComponent implements OnInit {

  plugin: PluginDetail | null = null;
  loading = true;
  activeTab = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pluginService: PluginService
  ) {}

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) { this.router.navigate(['/']); return; }

    this.pluginService.getPlugin(slug).subscribe({
      next: plugin => {
        this.plugin = plugin;
        this.loading = false;
      },
      error: () => this.router.navigate(['/'])
    });
  }

  formatStars(stars: number): string {
    if (stars >= 1000) return (stars / 1000).toFixed(1) + 'k';
    return stars?.toString() ?? '0';
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text);
  }

  getInstallSnippet(manager: 'lazy' | 'packer' | 'plug'): string {
    if (!this.plugin) return '';
    const repo = this.plugin.fullName;
    switch (manager) {
      case 'lazy':
        return `{ '${repo}' }`;
      case 'packer':
        return `use '${repo}'`;
      case 'plug':
        return `Plug '${repo}'`;
    }
  }
}