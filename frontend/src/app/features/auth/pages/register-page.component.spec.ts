import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RegisterPageComponent } from './register-page.component';

describe('RegisterPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterPageComponent],
      providers: [provideRouter([])]
    }).compileComponents();
  });

  it('should render the register form', () => {
    const fixture = TestBed.createComponent(RegisterPageComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h2')?.textContent).toContain('Abre tu espacio en CryptoFolio');
    expect(compiled.textContent).toContain('Crear cuenta');
    expect(compiled.querySelector('form')).toBeTruthy();
  });
});
