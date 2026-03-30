import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private isLight = false;

  constructor() {
    const saved = localStorage.getItem('neohub-theme');
    if (saved === 'light') {
      this.enableLight();
    }
  }

  get isLightMode(): boolean { return this.isLight; }

  toggle(): void {
    this.isLight ? this.enableDark() : this.enableLight();
  }

  private enableLight(): void {
    document.documentElement.setAttribute('data-theme', 'light');
    document.body.classList.add('light-mode');
    this.isLight = true;
    localStorage.setItem('neohub-theme', 'light');
  }

  private enableDark(): void {
    document.documentElement.removeAttribute('data-theme');
    document.body.classList.remove('light-mode');
    this.isLight = false;
    localStorage.setItem('neohub-theme', 'dark');
  }
}
