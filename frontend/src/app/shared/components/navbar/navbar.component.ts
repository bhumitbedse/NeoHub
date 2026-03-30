import { Component, HostListener, OnInit } from '@angular/core';
import { AuthService, User } from '../../../core/services/auth.service';
import { CommandPaletteService } from '../../../core/services/command-palette.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;

  constructor(
    public authService: AuthService,
    private cmdService: CommandPaletteService,
    public themeService: ThemeService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  @HostListener('document:keydown.control.k', ['$event'])
  @HostListener('document:keydown.meta.k', ['$event'])
  onCtrlK(e: KeyboardEvent): void {
    e.preventDefault();
    this.cmdService.open();
  }

  openCommandPalette(): void { this.cmdService.open(); }
  toggleTheme(): void        { this.themeService.toggle(); }
  login(): void              { this.authService.redirectToGitHub(); }
  logout(): void             { this.authService.logout(); }
}