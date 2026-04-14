import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/welcome/pages/welcome-page.component').then(
        (module) => module.WelcomePageComponent
      )
  }
];
