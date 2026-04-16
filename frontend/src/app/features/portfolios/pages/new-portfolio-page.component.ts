import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

interface CreatePortfolioRequest {
  name: string;
  description: string;
}

@Component({
  selector: 'app-new-portfolio-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './new-portfolio-page.component.html',
  styleUrl: './new-portfolio-page.component.css'
})
export class NewPortfolioPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  protected submitted = false;
  protected creating = false;
  protected successMessage = '';
  protected requestError = '';

  protected readonly portfolioForm = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(255)]]
  });

  protected async submitForm(): Promise<void> {
    this.submitted = true;
    this.requestError = '';
    this.successMessage = '';
    this.portfolioForm.markAllAsTouched();

    if (this.portfolioForm.invalid || this.creating) {
      return;
    }

    this.creating = true;

    const payload: CreatePortfolioRequest = {
      name: this.portfolioForm.controls.name.value?.trim() ?? '',
      description: this.portfolioForm.controls.description.value?.trim() ?? ''
    };

    this.http.post('/api/v1/portfolios', payload).subscribe({
      next: async () => {
        this.creating = false;
        this.successMessage = 'Portfolio creado correctamente.';
        await this.router.navigate(['/dashboard']);
      },
      error: (error: HttpErrorResponse) => {
        this.creating = false;
        this.requestError = this.mapRequestError(error);
      }
    });
  }

  protected showError(controlName: 'name' | 'description'): boolean {
    const control = this.portfolioForm.get(controlName);
    return !!control && control.invalid && (control.touched || this.submitted);
  }

  protected getControlError(controlName: 'name' | 'description'): string {
    const control = this.portfolioForm.get(controlName);

    if (!control || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'Este campo es obligatorio.';
    }

    if (control.errors['maxlength']) {
      return `No puede superar los ${control.errors['maxlength'].requiredLength} caracteres.`;
    }

    return 'Revisa este campo.';
  }

  private mapRequestError(error: HttpErrorResponse): string {
    if (error.status === 401) {
      return 'Necesitas un login real con JWT para crear portfolios. Ahora mismo el frontend todavía no guarda la sesión real.';
    }

    if (error.status === 400) {
      return 'El backend rechazó los datos del portfolio. Revisa nombre y descripción.';
    }

    if (error.status === 0) {
      return 'No se pudo conectar con el backend.';
    }

    return 'No se pudo crear el portfolio en este momento.';
  }
}
