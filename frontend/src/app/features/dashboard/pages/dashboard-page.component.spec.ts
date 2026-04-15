import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { DashboardPageComponent } from './dashboard-page.component';

describe('DashboardPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardPageComponent],
      providers: [provideRouter([]), provideHttpClientTesting()]
    }).compileComponents();
  });

  it('should render the dashboard shell', () => {
    const httpTestingController = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(DashboardPageComponent);
    fixture.detectChanges();

    httpTestingController.expectOne('/api/v1/prices');

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h1')?.textContent).toContain('Bienvenido de nuevo');
    expect(compiled.textContent).toContain('Balance total');
    expect(compiled.textContent).toContain('Portfolios');
    expect(compiled.textContent).toContain('Bitcoin');
  });
});
