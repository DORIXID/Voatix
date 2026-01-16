import { Component, signal } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonToggleModule } from '@angular/material/button-toggle';

@Component({
  selector: 'surveys-form-component',
  standalone: true,
  imports: [MatFormFieldModule,
    CommonModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule,
    MatButtonToggleModule],
  templateUrl: './surveys-form.component.html',
  styleUrls: ['./surveys-form.component.scss']
})

export class SurveysSidebarFormComponent {

  survey = {
    "id": 1,
    "title": "Какие столы закупать для веранды",
    "description": "Появилась необходимость заменить столы на веранде...",
    "startDate": "2026-01-07T10:00:00",
    "endDate": "2026-01-10T10:00:00",
    "type": "SINGLE_CHOICE",
    "totalVotes": 124,
    "points": [
      {
        "id": 10,
        "title": "Деревянные",
        "votes": 91,
        "percent": 73,
        "selectedByCurrentUser": true
      },
      {
        "id": 11,
        "title": "Стеклянные",
        "votes": 4,
        "percent": 3,
        "selectedByCurrentUser": false
      },
      {
        "id": 12,
        "title": "Металлические",
        "votes": 29,
        "percent": 24,
        "selectedByCurrentUser": false
      }
    ]
  }


  doNothing() { }


}