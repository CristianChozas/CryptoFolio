import { ChangeDetectorRef, Component, HostListener, NgZone, OnDestroy, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { timeout } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';

interface CoinPriceResponse {
  symbol: string;
  name: string;
  eurPrice: number;
  change24hPercent: number;
}

interface SummaryCard {
  label: string;
  value: string;
  trend: string;
  valueClass: string;
  trendClass: string;
  flashClass: string;
}

interface PortfolioResponse {
  id: number;
  name: string;
  description: string;
  createdAt: string;
}

interface PortfolioOverviewItemResponse {
  portfolio: PortfolioResponse;
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
  totalCurrentValue: number;
  totalCurrentValueCurrency: string;
  totalProfitLossAmount: number;
  totalProfitLossCurrency: string;
  portfolios: PortfolioOverviewItemResponse[];
  recentOperations: PortfolioOverviewOperationResponse[];
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css'
})
export class DashboardPageComponent implements OnInit, OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly ngZone = inject(NgZone);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  private readonly EUR_TO_USD = 1.18; // Tasa de conversión EUR a USD
  private eventSource: EventSource | null = null;
  private readonly flashTimeouts = new Map<string, ReturnType<typeof setTimeout>>();

  protected sidebarOpen = false;
  protected activeSidebarSection: 'summary' = 'summary' as const;
  protected username = 'Cristian';

  protected summaryCards: SummaryCard[] = [
    {
      label: 'Balance total',
      value: '—',
      trend: 'Cargando resumen…',
      valueClass: '',
      trendClass: '',
      flashClass: ''
    },
    {
      label: 'Bitcoin',
      value: '—',
      trend: 'Cargando…',
      valueClass: '',
      trendClass: '',
      flashClass: ''
    },
    {
      label: 'Ethereum',
      value: '—',
      trend: 'Cargando…',
      valueClass: '',
      trendClass: '',
      flashClass: ''
    }
  ];

  protected portfolioRows: PortfolioResponse[] = [];
  protected portfoliosLoading = true;
  protected portfoliosError = '';
  protected recentOperations: PortfolioOverviewOperationResponse[] = [];

  ngOnInit(): void {
    this.username = this.authService.getUsername() ?? 'Cristian';
    this.fetchOverview();
    this.fetchPrices();
    this.connectPriceStream();
  }

  ngOnDestroy(): void {
    this.eventSource?.close();

    for (const timeout of this.flashTimeouts.values()) {
      clearTimeout(timeout);
    }

    this.flashTimeouts.clear();
  }

  @HostListener('window:resize')
  protected handleResize(): void {
    if (window.innerWidth > 960) {
      this.sidebarOpen = false;
    }
  }

  protected toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  protected closeSidebar(): void {
    this.sidebarOpen = false;
  }

  protected openSummarySection(): void {
    this.activeSidebarSection = 'summary' as const;
    this.closeSidebar();
    this.scrollToSection('dashboard-summary-section');
  }

  protected openNewPortfolio(): void {
    this.closeSidebar();
    void this.router.navigate(['/portfolios/new']);
  }

  protected openNewOperation(): void {
    this.closeSidebar();
    void this.router.navigate(['/operations/new']);
  }

  protected get hasPortfolios(): boolean {
    return this.portfolioRows.length > 0;
  }

  protected get hasRecentOperations(): boolean {
    return this.recentOperations.length > 0;
  }

  protected logout(): void {
    this.authService.logout();
    this.closeSidebar();
    void this.router.navigate(['/']);
  }

  protected formatCurrency(value: number | null | undefined, currency = 'USD'): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency,
      maximumFractionDigits: 2
    }).format(value ?? 0);
  }

  protected formatPortfolioDate(value: string): string {
    return new Date(value).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  protected formatOperation(operation: PortfolioOverviewOperationResponse): string {
    const typeLabel = operation.type === 'BUY' ? 'Compra' : 'Venta';
    const amount = Number(operation.amount ?? 0).toLocaleString('es-ES', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 8
    });

    return `${typeLabel} de ${operation.crypto} · ${amount} en ${operation.portfolioName}`;
  }

  protected formatOperationDate(value: string): string {
    return new Date(value).toLocaleString('es-ES', {
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  private connectPriceStream(): void {
    if (typeof EventSource === 'undefined') {
      return;
    }

    this.eventSource?.close();
    this.eventSource = new EventSource('/api/v1/prices/stream');

    this.eventSource.addEventListener('prices', (event: MessageEvent<string>) => {
      this.ngZone.run(() => {
        const prices = JSON.parse(event.data) as CoinPriceResponse[];
        this.applyPrices(prices);
      });
    });

    this.eventSource.onerror = () => {
      this.ngZone.run(() => this.showUnavailablePrices());
    };
  }

  private fetchPrices(): void {
    this.http.get<CoinPriceResponse[]>('/api/v1/prices').subscribe({
      next: (prices) => this.applyPrices(prices),
      error: () => this.showUnavailablePrices()
    });
  }

  private fetchOverview(): void {
    this.portfoliosLoading = true;
    this.portfoliosError = '';
    this.recentOperations = [];

    this.http.get<PortfolioOverviewResponse>('/api/v1/portfolios/overview').pipe(timeout(8000)).subscribe({
      next: (overview) => {
        this.portfolioRows = overview.portfolios.map((item) => item.portfolio);
        this.recentOperations = overview.recentOperations;
        this.summaryCards = [
          {
            label: 'Balance total',
            value: this.formatCurrency(overview.totalCurrentValue, overview.totalCurrentValueCurrency || 'USD'),
            trend: `${overview.totalProfitLossAmount >= 0 ? '+' : ''}${this.formatCurrency(overview.totalProfitLossAmount, overview.totalProfitLossCurrency || 'USD')} P/L`,
            valueClass: '',
            trendClass: overview.totalProfitLossAmount >= 0 ? 'positive-text' : 'negative-text',
            flashClass: ''
          },
          this.summaryCards[1],
          this.summaryCards[2]
        ];
        this.portfoliosLoading = false;
      },
      error: (error) => {
        console.error('[CryptoFolio][Dashboard] overview:error', error);
        this.portfolioRows = [];
        this.recentOperations = [];
        this.portfoliosLoading = false;
        this.portfoliosError = error?.error?.message || 'No se pudo cargar el resumen real del dashboard.';
        this.summaryCards = [
          {
            label: 'Balance total',
            value: 'No disponible',
            trend: 'No se pudo cargar el resumen',
            valueClass: 'negative-text',
            trendClass: 'negative-text',
            flashClass: ''
          },
          this.summaryCards[1],
          this.summaryCards[2]
        ];
      }
    });
  }

  private scrollToSection(sectionId: string): void {
    if (typeof document === 'undefined') {
      return;
    }

    document.getElementById(sectionId)?.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });
  }

  private applyPrices(prices: CoinPriceResponse[]): void {
    if (prices.length === 0) {
      this.showUnavailablePrices();
      return;
    }

    const nextCards = [...this.summaryCards];

    for (const coin of prices) {
      if (coin.symbol === 'BTC') {
        nextCards[1] = this.buildPriceCard(coin, this.summaryCards[1]);
      } else if (coin.symbol === 'ETH') {
        nextCards[2] = this.buildPriceCard(coin, this.summaryCards[2]);
      }
    }

    this.summaryCards = nextCards;
    this.changeDetectorRef.detectChanges();
  }

  private buildPriceCard(coin: CoinPriceResponse, currentCard: SummaryCard): SummaryCard {
    const nextValue = `$${this.formatCoinPrice(coin)}`;
    const usdPrice = coin.eurPrice * this.EUR_TO_USD;
    const flashClass = this.resolveFlashClass(currentCard.value, usdPrice, coin.symbol);

    return {
      label: coin.name,
      value: nextValue,
      trend: `${coin.change24hPercent >= 0 ? '+' : ''}${coin.change24hPercent.toFixed(2)}% 24h`,
      valueClass: '',
      trendClass: coin.change24hPercent >= 0 ? 'positive-text' : 'negative-text',
      flashClass
    };
  }

  private formatCoinPrice(coin: CoinPriceResponse): string {
    const usdPrice = coin.eurPrice * this.EUR_TO_USD;
    return usdPrice.toLocaleString('es-ES', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  private resolveFlashClass(previousValue: string, nextPrice: number, symbol: string): string {
    const normalized = previousValue.replaceAll('$', '').replaceAll('.', '').replaceAll(',', '.');
    const previousPrice = Number(normalized);

    if (!Number.isFinite(previousPrice) || previousPrice === 0) {
      return '';
    }

    const isUp = nextPrice > previousPrice;
    const isDown = nextPrice < previousPrice;
    const flashClass = isUp ? 'flash-up' : isDown ? 'flash-down' : '';

    if (!flashClass) {
      return '';
    }

    const existingTimeout = this.flashTimeouts.get(symbol);
    if (existingTimeout) {
      clearTimeout(existingTimeout);
    }

    const timeoutRef = setTimeout(() => {
      const nextCards = [...this.summaryCards];
      const index = symbol === 'BTC' ? 1 : 2;
      nextCards[index] = {
        ...nextCards[index],
        flashClass: ''
      };
      this.summaryCards = nextCards;
      this.changeDetectorRef.detectChanges();
    }, 900);

    this.flashTimeouts.set(symbol, timeoutRef);

    return flashClass;
  }

  private showUnavailablePrices(): void {
    this.summaryCards = [
      this.summaryCards[0],
      {
        label: 'Bitcoin',
        value: 'No disponible',
        trend: 'No se pudo cargar el precio en vivo',
        valueClass: 'negative-text',
        trendClass: 'negative-text',
        flashClass: ''
      },
      {
        label: 'Ethereum',
        value: 'No disponible',
        trend: 'No se pudo cargar el precio en vivo',
        valueClass: 'negative-text',
        trendClass: 'negative-text',
        flashClass: ''
      }
    ];

    this.changeDetectorRef.detectChanges();
  }
}
