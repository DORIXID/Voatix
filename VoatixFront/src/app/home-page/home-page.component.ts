import { Component, signal } from '@angular/core';
// import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'home-page-component',
  // imports: [RouterOutlet],
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent {
  protected readonly title = signal('VoatixFront');
}