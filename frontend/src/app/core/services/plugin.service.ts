import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface Plugin {
  id: string;
  name: string;
  slug: string;
  fullName: string;
  description: string;
  categoryName: string;
  categorySlug: string;
  tags: string[];
  githubStars: number;
  githubUrl: string;
  isVerified: boolean;
  isColorscheme: boolean;
  lastCommitAt: string;
}

export interface PluginDetail extends Plugin {
  githubOwner: string;
  githubRepo: string;
  githubForks: number;
  openIssues: number;
  license: string;
  homepageUrl: string;
  neovimMinVersion: string;
  installGuide: string;
  configExample: string;
  keymapsSection: string;
  lastScrapedAt: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  icon: string;
}

@Injectable({ providedIn: 'root' })
export class PluginService {

  constructor(private api: ApiService) {}

  getPlugins(page = 0, size = 20, sortBy = 'githubStars'): Observable<PageResponse<Plugin>> {
    return this.api.get<PageResponse<Plugin>>('/plugins', { page, size, sortBy });
  }

  searchPlugins(query?: string, category?: string, page = 0, size = 20): Observable<PageResponse<Plugin>> {
    return this.api.get<PageResponse<Plugin>>('/plugins/search', {
      q: query, category, page, size
    });
  }

  getPlugin(slug: string): Observable<PluginDetail> {
    return this.api.get<PluginDetail>(`/plugins/${slug}`);
  }

  getCategories(): Observable<Category[]> {
    return this.api.get<Category[]>('/categories');
  }

  getColorschemes(page = 0, size = 20): Observable<PageResponse<Plugin>> {
    return this.api.get<PageResponse<Plugin>>('/plugins/colorschemes', { page, size });
  }
}