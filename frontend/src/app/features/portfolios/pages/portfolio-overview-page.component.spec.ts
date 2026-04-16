import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { PortfolioOverviewPageComponent } from './portfolio-overview-page.component';

describe('PortfolioOverviewPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioOverviewPageComponent],
      providers: [provideRouter([]), provideHttpClientTesting()]
    }).compileComponents();
  });

  it('should render aggregated portfolio overview', () => {
    const httpTestingController = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(PortfolioOverviewPageComponent);
    fixture.detectChanges();

    const request = httpTestingController.expectOne('/api/v1/portfolios/overview');
    request.flush({
      portfolioCount: 1,
      totalCurrentValue: 1250.5,
      totalCurrentValueCurrency: 'USD',
      totalProfitLossAmount: 250.25,
      totalProfitLossCurrency: 'USD',
      portfolios: [
        {
          portfolio: {
            id: 10,
            name: 'Main',
            description: 'Long term',
            createdAt: '2026-04-06T20:00:00Z'
          },
          balance: {
            BTC: 0.5
          },
          currentValue: 1250.5,
          currentValueCurrency: 'USD',
          profitLossAmount: 250.25,
          profitLossCurrency: 'USD',
          roiPercentage: 8.25
        }
      ],
      recentOperations: []
    });

    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h1')?.textContent).toContain('Resumen real de portfolios');
    expect(compiled.textContent).toContain('Balance total');
    expect(compiled.textContent).toContain('Main');
    expect(compiled.textContent).toContain('BTC');
  });
});
