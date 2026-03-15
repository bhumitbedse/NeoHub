import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Category, PluginService } from '../../../../core/services/plugin.service';

export interface FilterState {
  query: string;
  category: string;
  sortBy: string;
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

  sortOptions = [
    { value: 'githubStars', label: '⭐ Most Stars' },
    { value: 'createdAt', label: '🆕 Newest' },
    { value: 'name', label: '🔤 Name' },
  ];

  constructor(private pluginService: PluginService) {}

  ngOnInit(): void {
    this.pluginService.getCategories().subscribe(cats => {
      this.categories = cats;
    });
  }

  onCategorySelect(slug: string): void {
    this.selectedCategory = this.selectedCategory === slug ? '' : slug;
    this.emit();
  }

  onSortChange(): void {
    this.emit();
  }

  onSearch(): void {
    this.emit();
  }

  clearFilters(): void {
    this.selectedCategory = '';
    this.selectedSort = 'githubStars';
    this.searchQuery = '';
    this.emit();
  }

  private emit(): void {
    this.filterChanged.emit({
      query: this.searchQuery,
      category: this.selectedCategory,
      sortBy: this.selectedSort
    });
  }
}