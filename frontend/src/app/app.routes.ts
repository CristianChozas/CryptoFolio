import { Routes } from '@angular/router';

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
    loadComponent: () =>
      import('./features/dashboard/pages/dashboard-page.component').then(
        (module) => module.DashboardPageComponent
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
