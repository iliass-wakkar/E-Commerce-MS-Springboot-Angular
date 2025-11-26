import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive], // This line is correct, ensuring it's present.
  templateUrl: './dashboard.html',
})
export class DashboardComponent {

}
