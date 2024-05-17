import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ClubListComponent} from "./views/club-list/club-list.component";
import {LoggedIn} from './interceptors/logged-in.service';
import {LoginComponent} from './views/login/login.component';
import {AuthenticatedUserListComponent} from "./views/authenticated-user-list/authenticated-user-list.component";
import {PasswordsComponent} from "./views/passwords/passwords.component";

const routes: Routes = [
  {path: '', redirectTo: '/tournaments', pathMatch: 'full'},
  {path: 'login', component: LoginComponent},
  {path: 'clubs', component: ClubListComponent, canActivate: [LoggedIn]},
  {path: 'users', component: AuthenticatedUserListComponent, canActivate: [LoggedIn]},
  {path: 'passwords', component: PasswordsComponent, canActivate: [LoggedIn]},
  {
    path: 'participants',
    loadChildren: () => import('./views/participant-list/participant-list.module').then(m => m.ParticipantListModule),
    canActivate: [LoggedIn]
  },
  {
    path: 'tournaments',
    loadChildren: () => import('./views/tournament-list/tournament-list.module').then(m => m.TournamentListModule),
    canActivate: [LoggedIn]
  },
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
