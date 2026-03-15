import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PluginListComponent } from './pages/plugin-list/plugin-list.component';
import { PluginDetailComponent } from './pages/plugin-detail/plugin-detail.component';
import { PluginCardComponent } from './components/plugin-card/plugin-card.component';
import { FilterSidebarComponent } from './components/filter-sidebar/filter-sidebar.component';



@NgModule({
  declarations: [
    PluginListComponent,
    PluginDetailComponent,
    PluginCardComponent,
    FilterSidebarComponent
  ],
  imports: [
    CommonModule
  ]
})
export class PluginsModule { }
