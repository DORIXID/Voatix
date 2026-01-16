import { Component, signal } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

import { RouterModule } from '@angular/router';


@Component({
  selector: 'messages-form-component',
  standalone: true,
  imports: [MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule],
  templateUrl: './messages-form.component.html',
  styleUrls: ['./messages-form.component.scss']
})
export class MessagesSidebarFormComponent {

}