import { ChangeDetectorRef, Component, HostListener, NgZone, OnDestroy, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

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

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css'
})
export class DashboardPageComponent implements OnInit, OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly ngZone = inject(NgZone);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  private eventSource: EventSource | null = null;
  private readonly flashTimeouts = new Map<string, ReturnType<typeof setTimeout>>();

  protected sidebarOpen = false;
  protected username = 'Cristian';

  protected summaryCards: SummaryCard[] = [
    {
      label: 'Balance total',
      value: '€24,860.42',
      trend: '+€3,284.10 · +15.2%',
      valueClass: '',
      trendClass: 'positive-text',
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

  protected readonly activityItems = [
    'Compra registrada de BTC por €1,250.00',
    'Venta parcial de SOL con beneficio de +€142.20',
    'Actualización del resumen diario completada'
  ];

  ngOnInit(): void {
    this.username = this.authService.getUsername() ?? 'Cristian';
    this.fetchPortfolios();
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

  protected logout(): void {
    this.authService.logout();
    this.closeSidebar();
    void this.router.navigate(['/']);
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

  private fetchPortfolios(): void {
    this.portfoliosLoading = true;
    this.portfoliosError = '';

    this.http.get<PortfolioResponse[]>('/api/v1/portfolios').subscribe({
      next: (portfolios) => {
        this.portfolioRows = portfolios;
        this.portfoliosLoading = false;
      },
      error: () => {
        this.portfolioRows = [];
        this.portfoliosLoading = false;
        this.portfoliosError = 'No se pudieron cargar tus portfolios.';
      }
    });
  }

  protected formatPortfolioDate(value: string): string {
    return new Date(value).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
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
    const nextValue = `€${this.formatCoinPrice(coin)}`;

    const flashClass = this.resolveFlashClass(currentCard.value, coin.eurPrice, coin.symbol);

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
    return coin.eurPrice.toLocaleString('es-ES', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  private resolveFlashClass(previousValue: string, nextPrice: number, symbol: string): string {
    const normalized = previousValue.replace('€', '').replace(/\./g, '').replace(',', '.');
    const previousPrice = Number(normalized);

    if (!Number.isFinite(previousPrice) || previousPrice === 0) {
      return '';
    }

    const flashClass = nextPrice > previousPrice ? 'flash-up' : nextPrice < previousPrice ? 'flash-down' : '';

    if (!flashClass) {
      return '';
    }

    const existingTimeout = this.flashTimeouts.get(symbol);
    if (existingTimeout) {
      clearTimeout(existingTimeout);
    }

    const timeout = setTimeout(() => {
      const nextCards = [...this.summaryCards];
      const index = symbol === 'BTC' ? 1 : 2;
      nextCards[index] = {
        ...nextCards[index],
        flashClass: ''
      };
      this.summaryCards = nextCards;
      this.changeDetectorRef.detectChanges();
    }, 900);

    this.flashTimeouts.set(symbol, timeout);

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
