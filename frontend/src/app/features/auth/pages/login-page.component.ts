import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css'
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  protected submitted = false;
  protected loginReady = false;
  protected loginInProgress = false;
  protected requestError = '';
  protected challenge = this.generateChallenge();

  protected readonly loginForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    captchaAnswer: ['', [Validators.required]]
  });

  protected submitForm(): void {
    this.submitted = true;
    this.loginReady = false;
    this.requestError = '';
    this.loginForm.markAllAsTouched();

    if (this.loginForm.invalid || this.loginInProgress) {
      return;
    }

    const captchaControl = this.loginForm.get('captchaAnswer');
    const parsedAnswer = Number(captchaControl?.value);

    if (parsedAnswer !== this.challenge.answer) {
      captchaControl?.setErrors({ invalidCaptcha: true });
      return;
    }

    this.loginInProgress = true;

    this.authService.login({
      email: this.loginForm.controls.email.value ?? '',
      password: this.loginForm.controls.password.value ?? ''
    }).subscribe({
      next: () => {
        this.loginInProgress = false;
        this.loginReady = true;
        void this.router.navigate(['/dashboard']);
      },
      error: (error: HttpErrorResponse) => {
        this.loginInProgress = false;
        this.requestError = this.mapRequestError(error);
      }
    });
  }

  protected refreshChallenge(): void {
    this.challenge = this.generateChallenge();
    this.loginForm.get('captchaAnswer')?.reset('');
    this.loginForm.get('captchaAnswer')?.setErrors(null);
  }

  protected showError(controlName: 'email' | 'password' | 'captchaAnswer'): boolean {
    const control = this.loginForm.get(controlName);
    return !!control && control.invalid && (control.touched || this.submitted);
  }

  protected getControlError(controlName: 'email' | 'password' | 'captchaAnswer'): string {
    const control = this.loginForm.get(controlName);

    if (!control || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'Este campo es obligatorio.';
    }

    if (control.errors['email']) {
      return 'Introduce un email válido.';
    }

    if (control.errors['invalidCaptcha']) {
      return 'La verificación humana no es correcta.';
    }

    return 'Revisa este campo.';
  }

  private generateChallenge(): { left: number; right: number; answer: number } {
    const left = Math.floor(Math.random() * 8) + 1;
    const right = Math.floor(Math.random() * 8) + 1;

    return {
      left,
      right,
      answer: left + right
    };
  }

  private mapRequestError(error: HttpErrorResponse): string {
    if (error.status === 401) {
      return 'Credenciales inválidas.';
    }

    if (error.status === 400) {
      return 'Revisa los datos enviados.';
    }

    if (error.status === 0) {
      return 'No se pudo conectar con el backend.';
    }

    return 'No se pudo iniciar sesión.';
  }
}
