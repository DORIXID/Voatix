import { Routes } from '@angular/router';
import { LoginFormComponent } from './login-form/login-form.component';
import { RegistrationFormComponent } from './registration-form/registration-form.component';
import { MainSidebarFormComponent } from './main-sidebar-form/main-sidebar-form.component';
import { IdeasFormComponent } from './ideas-form/ideas-form.component';
import { SurveysSidebarFormComponent } from './surveys-form/surveys-form.component';
import path from 'path';
import { MessagesSidebarFormComponent } from './messages-form/messages-form.component';

export const routes: Routes = [
     { path: '', redirectTo: 'login', pathMatch: 'full'},
     { path: 'login', component: LoginFormComponent },
     { path: 'registration', component: RegistrationFormComponent},
     { path: 'main', component: MainSidebarFormComponent,
          children: [
               { path: 'ideas', component: IdeasFormComponent},
               { path: 'surveys', component: SurveysSidebarFormComponent},
               { path: 'messages', component: MessagesSidebarFormComponent}
          ]
     }
];
