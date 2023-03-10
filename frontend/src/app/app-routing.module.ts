import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ClubListComponent} from "./views/club-list/club-list.component";
import {LoggedInService} from './guards/logged-in.service';
import {LoginComponent} from './views/login/login.component';
import {ParticipantListComponent} from "./views/participant-list/participant-list.component";
import {TournamentListComponent} from "./views/tournament-list/tournament-list.component";
import {FightListComponent} from "./views/fight-list/fight-list.component";
import {AuthenticatedUserListComponent} from "./views/authenticated-user-list/authenticated-user-list.component";
import {PasswordsComponent} from "./views/passwords/passwords.component";
import {TournamentStatisticsComponent} from "./views/tournament-statistics/tournament-statistics.component";

const routes: Routes = [
  { path: '', redirectTo: '/tournaments', pathMatch: 'full' },
  {path: 'login', component: LoginComponent},
  {path: 'clubs', component: ClubListComponent, canActivate: [LoggedInService]},
  {path: 'users', component: AuthenticatedUserListComponent, canActivate: [LoggedInService]},
  {path: 'passwords', component: PasswordsComponent, canActivate: [LoggedInService]},
  {path: 'participants', component: ParticipantListComponent, canActivate: [LoggedInService]},
  {path: 'tournaments', component: TournamentListComponent, canActivate: [LoggedInService]},
  {path: 'tournaments/fights', component: FightListComponent, canActivate: [LoggedInService]},
  {path: 'tournaments/statistics', component: TournamentStatisticsComponent, canActivate: [LoggedInService]},
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
