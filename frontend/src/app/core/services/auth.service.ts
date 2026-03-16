import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable, tap } from "rxjs";
import { environment } from "../../../environments/environment";

export interface User {
  id: string;
  username: string;
  displayName: string;
  email: string;
  avatarUrl: string;
  githubUrl: string;
  role: string;
}

@Injectable({ providedIn: "root" })
export class AuthService {
  private base = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  // Use HttpClient directly — NOT ApiService, to avoid circular dependency
  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  get isLoggedIn(): boolean {
    return !!this.getToken();
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem("neohub_token");
  }

  loginWithCode(code: string): Observable<any> {
    return this.http
      .post<any>(`${this.base}/auth/github?code=${code}`, {})
      .pipe(
        tap((response) => {
          localStorage.setItem("neohub_token", response.accessToken);
          this.currentUserSubject.next(response.user);
        }),
      );
  }

  getMe(): Observable<User> {
    const token = this.getToken();
    return this.http
      .get<User>(`${this.base}/auth/me`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .pipe(tap((user) => this.currentUserSubject.next(user)));
  }

  logout(): void {
    localStorage.removeItem("neohub_token");
    this.currentUserSubject.next(null);
  }

  redirectToGitHub(): void {
    const params = new URLSearchParams({
      client_id: environment.githubClientId,
      scope: "read:user,user:email",
      redirect_uri: "http://localhost:4200/auth/callback",
    });
    window.location.href = `https://github.com/login/oauth/authorize?${params}`;
  }

  private loadUserFromStorage(): void {
    const token = this.getToken();
    if (token) {
      this.getMe().subscribe({
        error: () => this.logout(),
      });
    }
  }
}
