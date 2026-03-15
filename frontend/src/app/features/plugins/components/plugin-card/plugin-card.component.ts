import { Component, Input } from '@angular/core';
import { Plugin } from '../../../../core/services/plugin.service';

@Component({
  selector: 'app-plugin-card',
  templateUrl: './plugin-card.component.html',
  styleUrls: ['./plugin-card.component.scss']
})
export class PluginCardComponent {
  @Input() plugin!: Plugin;

  formatStars(stars: number): string {
    if (stars >= 1000) return (stars / 1000).toFixed(1) + 'k';
    return stars.toString();
  }
}