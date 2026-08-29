import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { DestinationsPage } from './pages/destinations/destinations';
import { EventDetailPage } from './pages/event-detail/event-detail';
import { EventsPage } from './pages/events/events';
import { LoginPage } from './pages/login/login';
import { ShellPage } from './pages/shell/shell';

export const routes: Routes = [
  { path: '', component: LoginPage },
  {
    path: 'app',
    component: ShellPage,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'events' },
      { path: 'destinations', component: DestinationsPage },
      { path: 'events', component: EventsPage },
      { path: 'events/:id', component: EventDetailPage },
    ],
  },
  { path: '**', redirectTo: '' },
];
