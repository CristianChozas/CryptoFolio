import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { timeout } from 'rxjs';

interface PortfolioResponse {
  id: number;
  name: string;
  description: string;
  createdAt: string;
}

interface PortfolioOverviewItemResponse {
  portfolio: PortfolioResponse;
  balance: Record<string, number>;
  currentValue: number;
  currentValueCurrency: string;
  profitLossAmount: number;
  profitLossCurrency: string;
  roiPercentage: number;
}

interface PortfolioOverviewOperationResponse {
  id: number;
  portfolioId: number;
  portfolioName: string;
  crypto: string;
  type: 'BUY' | 'SELL';
  amount: number;
  pricePerUnit: number;
  timestamp: string;
}

interface PortfolioOverviewResponse {
  portfolioCount: number;
  totalCurrentValue: number;
  totalCurrentValueCurrency: string;
  totalProfitLossAmount: number;
  totalProfitLossCurrency: string;
  portfolios: PortfolioOverviewItemResponse[];
  recentOperations: PortfolioOverviewOperationResponse[];
}

@Component({
  selector: 'app-portfolio-overview-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './portfolio-overview-page.component.html',
  styleUrl: './portfolio-overview-page.component.css'
})
export class PortfolioOverviewPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);

  protected loading = true;
  protected requestError = '';
  protected overview: PortfolioOverviewResponse | null = null;

  ngOnInit(): void {
    this.loadOverview();
  }

  protected get hasPortfolios(): boolean {
    return (this.overview?.portfolios.length ?? 0) > 0;
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

  protected formatDate(value: string): string {
    return new Date(value).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  protected balanceEntries(balance: Record<string, number> | null | undefined): Array<[string, number]> {
    return Object.entries(balance ?? {});
  }

  protected retry(): void {
    this.loadOverview();
  }

  private loadOverview(): void {
    this.loading = true;
    this.requestError = '';

    this.http.get<PortfolioOverviewResponse>('/api/v1/portfolios/overview').pipe(timeout(8000)).subscribe({
      next: (overview) => {
        this.overview = overview;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('[CryptoFolio][PortfoliosOverview] load:error', error);
        this.overview = null;
        this.loading = false;
        this.requestError = error?.error?.message || 'No se pudo cargar el resumen de portfolios.';
        this.cdr.markForCheck();
      }
    });
  }
}
