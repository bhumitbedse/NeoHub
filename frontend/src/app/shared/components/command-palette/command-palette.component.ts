import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { CommandPaletteService } from '../../../core/services/command-palette.service';
import { PluginService, Plugin } from '../../../core/services/plugin.service';

interface PaletteItem {
  type: 'page' | 'plugin';
  label: string;
  sub?: string;
  icon: string;
  route?: string[];
  slug?: string;
}

@Component({
  selector: 'app-command-palette',
  templateUrl: './command-palette.component.html',
  styleUrls: ['./command-palette.component.scss']
})
export class CommandPaletteComponent implements OnInit, OnDestroy {
  isOpen = false;
  query = '';
  activeIndex = 0;
  results: PaletteItem[] = [];

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  private pages: PaletteItem[] = [
    { type: 'page', label: 'Plugins', sub: 'Browse all plugins',     icon: 'extension',    route: ['/'] },
    { type: 'page', label: 'Themes',  sub: 'Color themes & schemes', icon: 'palette',      route: ['/themes'] },
    { type: 'page', label: 'Configs', sub: 'Neovim configurations',  icon: 'settings',     route: ['/configs'] },
  ];

  constructor(
    private cmdService: CommandPaletteService,
    private pluginService: PluginService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cmdService.isOpen$.pipe(takeUntil(this.destroy$)).subscribe(open => {
      this.isOpen = open;
      if (open) {
        this.query = '';
        this.results = [...this.pages];
        this.activeIndex = 0;
        setTimeout(() => document.getElementById('cmd-input')?.focus(), 50);
      }
    });

    this.searchSubject.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(q => this.performSearch(q));
  }

  onQueryChange(): void {
    this.activeIndex = 0;
    this.searchSubject.next(this.query);
  }

  private performSearch(q: string): void {
    if (!q.trim()) {
      this.results = [...this.pages];
      return;
    }
    const lower = q.toLowerCase();
    const filtered = this.pages.filter(p => this.fuzzyMatch(p.label, lower) || (p.sub && this.fuzzyMatch(p.sub, lower)));
    this.pluginService.searchPlugins(q, '', 0, 6).subscribe({
      next: res => {
        const pluginItems: PaletteItem[] = res.content.map(p => ({
          type: 'plugin',
          label: p.name,
          sub: p.description,
          icon: 'extension',
          slug: p.slug
        }));
        this.results = [...filtered, ...pluginItems];
      },
      error: () => { this.results = [...filtered]; }
    });
  }

  @HostListener('document:keydown', ['$event'])
  onKeyDown(e: KeyboardEvent): void {
    if (!this.isOpen) return;
    if (e.key === 'Escape') { this.close(); return; }
    if (e.key === 'ArrowDown') { 
      e.preventDefault(); 
      this.activeIndex = Math.min(this.activeIndex + 1, Math.max(0, this.results.length - 1)); 
      this.scrollToActive();
    }
    if (e.key === 'ArrowUp') { 
      e.preventDefault(); 
      this.activeIndex = Math.max(this.activeIndex - 1, 0); 
      this.scrollToActive();
    }
    if (e.key === 'Enter') { 
      e.preventDefault();
      this.select(this.results[this.activeIndex]); 
    }
  }

  private scrollToActive(): void {
    setTimeout(() => {
      const activeEl = document.querySelector('.cmd-item.active');
      if (activeEl) activeEl.scrollIntoView({ block: 'nearest' });
    });
  }

  private fuzzyMatch(str: string, pattern: string): boolean {
    str = str.toLowerCase();
    pattern = pattern.toLowerCase();
    let pIdx = 0;
    let sIdx = 0;
    while (pIdx < pattern.length && sIdx < str.length) {
      if (pattern[pIdx] === str[sIdx]) pIdx++;
      sIdx++;
    }
    return pIdx === pattern.length;
  }

  select(item: PaletteItem): void {
    if (!item) return;
    if (item.slug) this.router.navigate(['/plugins', item.slug]);
    else if (item.route) this.router.navigate(item.route);
    this.close();
  }

  close(): void { this.cmdService.close(); }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
}
