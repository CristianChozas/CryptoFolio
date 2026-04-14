import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SiteFooterComponent } from './shared/components/site-footer/site-footer.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SiteFooterComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
