import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-auth-callback',
  template: `
    <div style="display:flex;justify-content:center;
                align-items:center;height:80vh;flex-direction:column;gap:16px">
      <mat-spinner></mat-spinner>
      <p style="color:#94a3b8">Signing you in...</p>
    </div>
  `
})
export class AuthCallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');
    if (!code) {
      this.router.navigate(['/']);
      return;
    }

    this.authService.loginWithCode(code).subscribe({
      next: () => {
        this.snackBar.open('Welcome to NeoHub! 🎉', '', { duration: 3000 });
        this.router.navigate(['/']);
      },
      error: () => {
        this.snackBar.open('Login failed. Please try again.', '', { duration: 3000 });
        this.router.navigate(['/']);
      }
    });
  }
}