import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ClubListComponent} from "./views/club-list/club-list.component";
import {LoggedIn} from './interceptors/logged-in.service';
import {AuthenticatedUserListComponent} from "./views/authenticated-user-list/authenticated-user-list.component";
import {PasswordsComponent} from "./views/passwords/passwords.component";
import {ParticipantStatisticsComponent} from "./views/participant-statistics/participant-statistics.component";
import {ParticipantFightListComponent} from "./views/participant-fight-list/participant-fight-list.component";
import {RedirectGuard} from "./components/navigation/redirect-guard/redirect.guard";

const routes: Routes = [
  {path: '', redirectTo: '/tournaments', pathMatch: 'full'},
  {
    path: 'login',
    loadChildren: () => import('./views/login/login.module').then(m => m.LoginModule),
  },
  {path: 'registry/clubs', component: ClubListComponent, canActivate: [LoggedIn]},
  {
    path: 'registry/participants',
    loadChildren: () => import('./views/participant-list/participant-list.module').then(m => m.ParticipantListModule),
    canActivate: [LoggedIn]
  },
  {
    path: 'tournaments',
    loadChildren: () => import('./views/tournament-list/tournament-list.module').then(m => m.TournamentListModule),
    canActivate: [LoggedIn]
  },
  {path: 'administration/users', component: AuthenticatedUserListComponent, canActivate: [LoggedIn]},
  {path: 'passwords', component: PasswordsComponent, canActivate: [LoggedIn]},
  {path: 'participants/statistics', component: ParticipantStatisticsComponent, canActivate: [LoggedIn]},
  {path: 'participants/fights', component: ParticipantFightListComponent, canActivate: [LoggedIn]},
  {
    path: 'help/wiki',
    canActivate: [RedirectGuard],
    component: RedirectGuard,
    data: {
      externalUrl: "https://github.com/softwaremagico/KendoTournamentManager/wiki"
    }
  },
  {
    path: 'help/about',
    canActivate: [RedirectGuard],
    component: RedirectGuard,
    data: {
      externalUrl: "https://github.com/softwaremagico/KendoTournamentManager"
    }
  },
  {
    path: 'help/license',
    canActivate: [RedirectGuard],
    component: RedirectGuard,
    data: {
      externalUrl: "https://github.com/softwaremagico/KendoTournamentManager/wiki/Third-Party-Tools"
    }
  },
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
