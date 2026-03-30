import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Category, PluginService } from '../../../../core/services/plugin.service';

export interface FilterState {
  query: string;
  category: string;
  sortBy: string;
  tags: string[];
  minStars: number;
}

@Component({
  selector: 'app-filter-sidebar',
  templateUrl: './filter-sidebar.component.html',
  styleUrls: ['./filter-sidebar.component.scss']
})
export class FilterSidebarComponent implements OnInit {
  @Output() filterChanged = new EventEmitter<FilterState>();

  categories: Category[] = [];
  selectedCategory = '';
  selectedSort = 'githubStars';
  searchQuery = '';
  selectedTags: string[] = [];
  minStars = 0;

  collapsedSections: Record<string, boolean> = {
    categories: false,
    tags: false
  };

  sortOptions = [
    { value: 'githubStars', label: '⭐ Stars' },
    { value: 'createdAt',   label: '🆕 Newest' },
    { value: 'name',        label: '🔤 A–Z' },
  ];

  starOptions = [
    { value: 0,    label: 'All' },
    { value: 100,  label: '100+' },
    { value: 500,  label: '500+' },
    { value: 1000, label: '1k+' },
  ];

  popularTags = ['LSP', 'UI', 'Git', 'Treesitter', 'Completion', 'Fuzzy', 'Syntax', 'Debug', 'File', 'Theme'];

  constructor(private pluginService: PluginService) {}

  ngOnInit(): void {
    this.pluginService.getCategories().subscribe(cats => {
      this.categories = cats;
    });
  }

  get hasActiveFilters(): boolean {
    return !!(this.selectedCategory || this.searchQuery || this.selectedTags.length || this.minStars);
  }

  toggleSection(key: string): void {
    this.collapsedSections[key] = !this.collapsedSections[key];
  }

  toggleTag(tag: string): void {
    const idx = this.selectedTags.indexOf(tag);
    if (idx >= 0) this.selectedTags.splice(idx, 1);
    else this.selectedTags.push(tag);
    this.emit();
  }

  setMinStars(val: number): void {
    this.minStars = val;
    this.emit();
  }

  onCategorySelect(slug: string): void {
    this.selectedCategory = this.selectedCategory === slug ? '' : slug;
    this.emit();
  }

  onSortChange(): void  { this.emit(); }
  onSearch(): void      { this.emit(); }

  clearFilters(): void {
    this.selectedCategory = '';
    this.selectedSort = 'githubStars';
    this.searchQuery = '';
    this.selectedTags = [];
    this.minStars = 0;
    this.emit();
  }

  private emit(): void {
    this.filterChanged.emit({
      query: this.searchQuery,
      category: this.selectedCategory,
      sortBy: this.selectedSort,
      tags: [...this.selectedTags],
      minStars: this.minStars
    });
  }
}