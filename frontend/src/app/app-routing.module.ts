import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PluginListComponent } from './features/plugins/pages/plugin-list/plugin-list.component';
import { PluginDetailComponent } from './features/plugins/pages/plugin-detail/plugin-detail.component';
import { AuthCallbackComponent } from './features/auth/pages/auth-callback/auth-callback.component';

const routes: Routes = [
  { path: '', component: PluginListComponent },
  { path: 'plugins/:slug', component: PluginDetailComponent },
  { path: 'auth/callback', component: AuthCallbackComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}