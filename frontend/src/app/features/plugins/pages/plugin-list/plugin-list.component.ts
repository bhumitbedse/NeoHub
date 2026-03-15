import { Component, OnInit } from '@angular/core';
import { Plugin, PluginService, PageResponse } from '../../../../core/services/plugin.service';
import { FilterState } from '../../components/filter-sidebar/filter-sidebar.component';
import { debounceTime, Subject, switchMap } from 'rxjs';

@Component({
  selector: 'app-plugin-list',
  templateUrl: './plugin-list.component.html',
  styleUrls: ['./plugin-list.component.scss']
})
export class PluginListComponent implements OnInit {

  plugins: Plugin[] = [];
  totalElements = 0;
  currentPage = 0;
  pageSize = 20;
  loading = true;

  currentFilter: FilterState = {
    query: '',
    category: '',
    sortBy: 'githubStars'
  };

  private filterSubject = new Subject<FilterState>();

  constructor(private pluginService: PluginService) {}

  ngOnInit(): void {
    this.loadPlugins();

    // Debounce filter changes
    this.filterSubject.pipe(
      debounceTime(300),
      switchMap(filter => {
        this.loading = true;
        this.currentPage = 0;
        if (filter.query || filter.category) {
          return this.pluginService.searchPlugins(
            filter.query, filter.category, 0, this.pageSize);
        }
        return this.pluginService.getPlugins(0, this.pageSize, filter.sortBy);
      })
    ).subscribe(response => {
      this.plugins = response.content;
      this.totalElements = response.totalElements;
      this.loading = false;
    });
  }

  loadPlugins(): void {
    this.loading = true;
    this.pluginService.getPlugins(this.currentPage, this.pageSize, 'githubStars')
      .subscribe(response => {
        this.plugins = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      });
  }

  onFilterChanged(filter: FilterState): void {
    this.currentFilter = filter;
    this.filterSubject.next(filter);
  }

  loadMore(): void {
    this.currentPage++;
    const obs = this.currentFilter.query || this.currentFilter.category
      ? this.pluginService.searchPlugins(
          this.currentFilter.query,
          this.currentFilter.category,
          this.currentPage, this.pageSize)
      : this.pluginService.getPlugins(
          this.currentPage, this.pageSize,
          this.currentFilter.sortBy);

    obs.subscribe(response => {
      this.plugins = [...this.plugins, ...response.content];
      this.totalElements = response.totalElements;
    });
  }

  get hasMore(): boolean {
    return this.plugins.length < this.totalElements;
  }
}