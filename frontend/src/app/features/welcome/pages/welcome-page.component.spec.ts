import { TestBed } from '@angular/core/testing';
import { WelcomePageComponent } from './welcome-page.component';

describe('WelcomePageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WelcomePageComponent]
    }).compileComponents();
  });

  it('should render the frontend bootstrap heading', () => {
    const fixture = TestBed.createComponent(WelcomePageComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h1')?.textContent).toContain('CryptoFolio');
    expect(compiled.textContent).toContain('Ticket #601');
  });
});
