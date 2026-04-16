import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { firstValueFrom, timeout } from 'rxjs';

interface PortfolioResponse {
  id: number;
  name: string;
  description: string;
  createdAt: string;
}

interface PortfolioSummaryResponse {
  portfolio: PortfolioResponse;
  balance: Record<string, number>;
  profitLossAmount: number;
  profitLossCurrency: string;
  roiPercentage: number;
}

interface CreateTransactionRequest {
  portfolioId: number;
  crypto: string;
  type: 'BUY' | 'SELL';
  amount: number;
  pricePerUnit: number;
}

@Component({
  selector: 'app-new-transaction-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './new-transaction-page.component.html',
  styleUrl: './new-transaction-page.component.css'
})
export class NewTransactionPageComponent implements OnInit {
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly http = inject(HttpClient);

  protected submitted = false;
  protected loadingPortfolios = true;
  protected loadingSummary = false;
  protected creatingTransaction = false;
  protected successMessage = '';
  protected requestError = '';
  protected portfolioLoadError = '';
  protected portfolios: PortfolioResponse[] = [];
  protected selectedSummary: PortfolioSummaryResponse | null = null;
  protected selectedPortfolioId: number | null = null;
  protected selectionSubmitted = false;
  protected showTransactionStep = false;

  protected readonly transactionForm = this.formBuilder.group({
    portfolioId: [null as number | null, [Validators.required]],
    type: ['BUY' as 'BUY' | 'SELL', [Validators.required]],
    crypto: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(10)]],
    amount: [null as number | null, [Validators.required, Validators.min(0.00000001)]],
    pricePerUnit: [null as number | null, [Validators.required, Validators.min(0.01)]]
  });

  async ngOnInit(): Promise<void> {
    await this.loadPortfolios();
  }

  protected get hasPortfolios(): boolean {
    return this.portfolios.length > 0;
  }

  protected selectPortfolio(portfolioId: string): void {
    this.selectedPortfolioId = portfolioId ? Number(portfolioId) : null;
    this.selectionSubmitted = false;
    this.successMessage = '';
    this.requestError = '';
  }

  protected continueToTransaction(): void {
    this.selectionSubmitted = true;
    this.successMessage = '';
    this.requestError = '';

    if (!this.selectedPortfolioId || this.loadingSummary) {
      return;
    }

    this.transactionForm.controls.portfolioId.setValue(this.selectedPortfolioId);
    this.showTransactionStep = true;
    this.loadPortfolioSummary(this.selectedPortfolioId);
  }

  protected goBackToSelection(): void {
    this.showTransactionStep = false;
    this.successMessage = '';
    this.requestError = '';
  }

  protected async retryLoadPortfolios(): Promise<void> {
    await this.loadPortfolios();
  }

  protected get showPortfolioSelectionError(): boolean {
    return this.selectionSubmitted && !this.selectedPortfolioId;
  }

  protected submitForm(): void {
    this.submitted = true;
    this.successMessage = '';
    this.requestError = '';
    this.transactionForm.markAllAsTouched();

    if (this.transactionForm.invalid || this.creatingTransaction) {
      return;
    }

    this.creatingTransaction = true;

    const payload: CreateTransactionRequest = {
      portfolioId: this.transactionForm.controls.portfolioId.value ?? 0,
      type: this.transactionForm.controls.type.value ?? 'BUY',
      crypto: (this.transactionForm.controls.crypto.value ?? '').trim().toUpperCase(),
      amount: this.transactionForm.controls.amount.value ?? 0,
      pricePerUnit: this.transactionForm.controls.pricePerUnit.value ?? 0
    };

    this.http.post('/api/v1/transactions', payload).subscribe({
      next: () => {
        this.creatingTransaction = false;
        this.successMessage = 'Operación registrada correctamente.';
        this.submitted = false;
        this.transactionForm.reset({
          portfolioId: this.transactionForm.controls.portfolioId.value,
          type: 'BUY',
          crypto: '',
          amount: null,
          pricePerUnit: null
        });
        this.transactionForm.markAsPristine();
        this.transactionForm.markAsUntouched();

        const portfolioId = this.transactionForm.controls.portfolioId.value;
        if (portfolioId) {
          void this.loadPortfolioSummary(portfolioId);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.creatingTransaction = false;
        this.requestError = this.mapTransactionError(error);
      }
    });
  }

  protected showError(controlName: 'portfolioId' | 'type' | 'crypto' | 'amount' | 'pricePerUnit'): boolean {
    const control = this.transactionForm.get(controlName);
    return !!control && control.invalid && (control.touched || this.submitted);
  }

  protected getControlError(controlName: 'portfolioId' | 'type' | 'crypto' | 'amount' | 'pricePerUnit'): string {
    const control = this.transactionForm.get(controlName);

    if (!control || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'Este campo es obligatorio.';
    }

    if (control.errors['minlength']) {
      return `Debe tener al menos ${control.errors['minlength'].requiredLength} caracteres.`;
    }

    if (control.errors['maxlength']) {
      return `No puede superar los ${control.errors['maxlength'].requiredLength} caracteres.`;
    }

    if (control.errors['min']) {
      return 'Introduce un valor positivo válido.';
    }

    return 'Revisa este campo.';
  }

  protected formatCurrency(value: number | null | undefined, currency = 'USD'): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency,
      maximumFractionDigits: 2
    }).format(value ?? 0);
  }

  protected formatPercentage(value: number | null | undefined): string {
    return `${(value ?? 0).toFixed(2)}%`;
  }

  protected balanceEntries(): Array<[string, number]> {
    return Object.entries(this.selectedSummary?.balance ?? {});
  }

  private async loadPortfolios(): Promise<void> {
    this.loadingPortfolios = true;
    this.portfolioLoadError = '';
    this.portfolios = [];

    try {
      const portfolios = await firstValueFrom(
        this.http.get<PortfolioResponse[]>('/api/v1/portfolios').pipe(timeout(8000))
      );

      this.portfolios = portfolios;
      this.selectedPortfolioId = portfolios[0]?.id ?? null;
    } catch {
      this.portfolioLoadError = 'No se pudieron cargar tus portfolios.';
      this.selectedPortfolioId = null;
      console.error('[CryptoFolio][NewOperation] load portfolios:error');
    } finally {
      this.loadingPortfolios = false;
      this.changeDetectorRef.detectChanges();
    }
  }

  private async loadPortfolioSummary(portfolioId: number): Promise<void> {
    this.loadingSummary = true;

    try {
      this.selectedSummary = await firstValueFrom(
        this.http.get<PortfolioSummaryResponse>(`/api/v1/portfolios/${portfolioId}/summary`).pipe(timeout(8000))
      );
    } catch {
      this.selectedSummary = null;
      this.requestError = 'No se pudo cargar el resumen del portfolio seleccionado.';
      console.error('[CryptoFolio][NewOperation] load summary:error', { portfolioId });
    } finally {
      this.loadingSummary = false;
      this.changeDetectorRef.detectChanges();
    }
  }

  private mapTransactionError(error: HttpErrorResponse): string {
    if (error.status === 400) {
      return 'El backend rechazó la operación. Revisa tipo, cantidad y precio.';
    }

    if (error.status === 403) {
      return 'No puedes operar sobre ese portfolio.';
    }

    if (error.status === 404) {
      return 'El portfolio seleccionado ya no existe.';
    }

    if (error.status === 0) {
      return 'No se pudo conectar con el backend.';
    }

    return 'No se pudo registrar la operación.';
  }
}
