import { Routes } from '@angular/router';
import { LoginFormComponent } from './login-form/login-form.component';
import { RegistrationFormComponent } from './registration-form/registration-form.component';
import { MainSidebarFormComponent } from './main-sidebar-form/main-sidebar-form.component';
import { IdeasFormComponent } from './ideas-form/ideas-form.component';
import { AuthGuard } from './service/authorization/auth.guard';
import { SurveysSidebarFormComponent } from './surveys-form/surveys-form.component';
import path from 'path';
import { MessagesFormComponent } from './messages-form/messages-form.component';
import { IdeaCreateFormComponent } from './idea-create-form/idea-create-form.component';
import { IdeaViewComponent } from './idea-view/idea-view.component';
import { SurveyCreateFormComponent } from './survey-create-form/survey-create-form.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { FAQComponent } from './faq/faq.component';
import { ProjectSettingsComponent } from './project-settings/project-settings.component';

export const routes: Routes = [
     { path: '', redirectTo: 'login', pathMatch: 'full'},
     { path: 'login', component: LoginFormComponent },
     { path: 'registration', component: RegistrationFormComponent},
     { path: 'main', component: MainSidebarFormComponent, canActivate: [AuthGuard], canActivateChild: [AuthGuard],
          children: [
               { path: 'ideas/create', component: IdeaCreateFormComponent },
               { path: 'ideas/view/:id', component: IdeaViewComponent },
               { path: 'ideas', component: IdeasFormComponent},
               { path: 'ideas/:projectId', component: IdeasFormComponent },
               { path: 'surveys/create', component: SurveyCreateFormComponent },
               { path: 'surveys', component: SurveysSidebarFormComponent},
               { path: 'surveys/:projectId', component: SurveysSidebarFormComponent },
               { path: 'messages', component: MessagesFormComponent},
               { path: 'profile', component: UserProfileComponent },
               { path: 'faq', component: FAQComponent },
               { path: 'project-settings/:projectId', component: ProjectSettingsComponent }
          ]
     }
];
