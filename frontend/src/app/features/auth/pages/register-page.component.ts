import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.css'
})
export class RegisterPageComponent implements OnDestroy {
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  protected submitted = false;
  protected accountCreated = false;
  protected creatingAccount = false;
  protected requestError = '';
  private redirectTimeoutId: ReturnType<typeof setTimeout> | null = null;

  protected readonly registerForm = this.formBuilder.group(
    {
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
      acceptedTerms: [false, [Validators.requiredTrue]]
    },
    { validators: this.passwordsMatchValidator }
  );

  protected submitForm(): void {
    this.submitted = true;
    this.accountCreated = false;
    this.requestError = '';
    this.registerForm.markAllAsTouched();

    if (this.registerForm.invalid || this.creatingAccount) {
      return;
    }

    this.creatingAccount = true;

    this.authService.register({
      username: this.registerForm.controls.username.value ?? '',
      email: this.registerForm.controls.email.value ?? '',
      password: this.registerForm.controls.password.value ?? ''
    }).subscribe({
      next: () => {
        this.creatingAccount = false;
        this.accountCreated = true;
        this.redirectTimeoutId = setTimeout(() => {
          void this.router.navigate(['/']);
        }, 2500);
      },
      error: (error: HttpErrorResponse) => {
        this.creatingAccount = false;
        this.requestError = this.mapRequestError(error);
      }
    });
  }

  protected showError(controlName: 'username' | 'email' | 'password' | 'confirmPassword' | 'acceptedTerms'): boolean {
    const control = this.registerForm.get(controlName);
    return !!control && control.invalid && (control.touched || this.submitted);
  }

  protected getControlError(controlName: 'username' | 'email' | 'password' | 'confirmPassword' | 'acceptedTerms'): string {
    const control = this.registerForm.get(controlName);

    if (!control || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'Este campo es obligatorio.';
    }

    if (control.errors['email']) {
      return 'Introduce un email válido.';
    }

    if (control.errors['minlength']) {
      return `Debe tener al menos ${control.errors['minlength'].requiredLength} caracteres.`;
    }

    if (control.errors['maxlength']) {
      return `No puede superar los ${control.errors['maxlength'].requiredLength} caracteres.`;
    }

    if (control.errors['requiredTrue']) {
      return 'Debes aceptar los términos para continuar.';
    }

    if (controlName === 'confirmPassword' && this.registerForm.errors?.['passwordMismatch']) {
      return 'Las contraseñas no coinciden.';
    }

    return 'Revisa este campo.';
  }

  private passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;

    if (!password || !confirmPassword) {
      return null;
    }

    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  private mapRequestError(error: HttpErrorResponse): string {
    if (error.status === 409) {
      return 'Correo ya existente.';
    }

    if (error.status === 400) {
      return 'Revisa los datos enviados.';
    }

    if (error.status === 0) {
      return 'No se pudo conectar con el backend.';
    }

    return 'No se pudo crear la cuenta.';
  }

  ngOnDestroy(): void {
    if (this.redirectTimeoutId) {
      clearTimeout(this.redirectTimeoutId);
    }
  }
}
