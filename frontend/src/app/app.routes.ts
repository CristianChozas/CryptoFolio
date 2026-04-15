import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/welcome/pages/welcome-page.component').then(
        (module) => module.WelcomePageComponent
      )
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/pages/dashboard-page.component').then(
        (module) => module.DashboardPageComponent
      )
  },
  {
    path: 'portfolios/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/portfolios/pages/new-portfolio-page.component').then(
        (module) => module.NewPortfolioPageComponent
      )
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login-page.component').then(
        (module) => module.LoginPageComponent
      )
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/pages/register-page.component').then(
        (module) => module.RegisterPageComponent
      )
  }
];
