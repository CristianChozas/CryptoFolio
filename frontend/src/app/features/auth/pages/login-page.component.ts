import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

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

  protected submitted = false;
  protected loginReady = false;
  protected challenge = this.generateChallenge();

  protected readonly loginForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    captchaAnswer: ['', [Validators.required]]
  });

  protected submitForm(): void {
    this.submitted = true;
    this.loginReady = false;
    this.loginForm.markAllAsTouched();

    if (this.loginForm.invalid) {
      return;
    }

    const captchaControl = this.loginForm.get('captchaAnswer');
    const parsedAnswer = Number(captchaControl?.value);

    if (parsedAnswer !== this.challenge.answer) {
      captchaControl?.setErrors({ invalidCaptcha: true });
      return;
    }

    this.loginReady = true;
    void this.router.navigate(['/dashboard']);
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
}
