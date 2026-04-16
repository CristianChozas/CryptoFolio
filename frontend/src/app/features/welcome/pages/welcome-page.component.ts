import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-welcome-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './welcome-page.component.html',
  styleUrl: './welcome-page.component.css'
})
export class WelcomePageComponent {
  protected readonly summaryMetrics = [
    { label: 'Control de rentabilidad', value: 'ROI', detail: 'consulta el rendimiento y beneficio/pérdida de cada portfolio' },
    { label: 'Visión por portfolio', value: 'Multi', detail: 'organiza tus inversiones por estrategia, objetivo o perfil de riesgo' },
    { label: 'Seguimiento activo', value: '24/7', detail: 'visualiza balances y movimientos en una interfaz clara y directa' }
  ];

  protected readonly featureCards = [
    {
      title: 'Centraliza tus portfolios',
      description:
        'Agrupa tus inversiones por estrategia y entiende cada portfolio como una unidad con identidad propia.'
    },
    {
      title: 'Registra compras y ventas',
      description:
        'Cada transacción actualiza tu posición y te ayuda a entender cómo evoluciona tu cartera con el tiempo.'
    },
    {
      title: 'Visualiza el estado actual',
      description:
        'Consulta balances, rendimiento y resultados de forma inmediata, sin perderte entre pantallas innecesarias.'
    }
  ];

  protected readonly productHighlights = [
    'Diseñada para entender rápidamente qué tienes, cómo va y dónde están tus resultados.',
    'Pensada para usuarios que quieren claridad financiera sin complejidad visual excesiva.',
    'Con una estética sobria y premium para transmitir confianza desde la primera pantalla.'
  ];

  protected readonly targetUsers = [
    'Inversores que quieren una visión más ordenada de sus carteras crypto.',
    'Usuarios que necesitan entender rendimiento y exposición sin hojas de cálculo.',
    'Personas que valoran una experiencia limpia, seria y fácil de interpretar.'
  ];
}
