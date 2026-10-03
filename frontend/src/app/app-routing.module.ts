import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ClubListComponent} from "./views/club-list/club-list.component";
import {LoggedInService} from './interceptors/logged-in.service';
import {AuthenticatedUserListComponent} from "./views/authenticated-user-list/authenticated-user-list.component";
import {PasswordsComponent} from "./views/passwords/passwords.component";
import {ParticipantStatisticsComponent} from "./views/participant-statistics/participant-statistics.component";
import {ParticipantFightListComponent} from "./views/participant-fight-list/participant-fight-list.component";
import {RedirectGuard} from "./components/navigation/redirect-guard/redirect.guard";
import {TenantListComponent} from './views/tenant-list/tenant-list.component';
import {SuperAdminGuardService} from './services/super-admin-guard.service';

const routes: Routes = [
  {path: '', redirectTo: '/tournaments', pathMatch: 'full'},
  {
    path: 'login',
    loadChildren: () => import('./views/login/login.module').then(m => m.LoginModule),
  },
  {path: 'registry/clubs', component: ClubListComponent, canActivate: [LoggedInService]},
  {
    path: 'registry/participants',
    loadChildren: () => import('./views/participant-list/participant-list.module').then(m => m.ParticipantListModule),
    canActivate: [LoggedInService]
  },
  {
    path: 'tournaments',
    loadChildren: () => import('./views/tournament-list/tournament-list.module').then(m => m.TournamentListModule),
    canActivate: [LoggedInService]
  },
  {path: 'administration/users', component: AuthenticatedUserListComponent, canActivate: [LoggedInService]},
  {path: 'administration/tenants', component: TenantListComponent, canActivate: [LoggedInService, SuperAdminGuardService]},
  {path: 'passwords', component: PasswordsComponent, canActivate: [LoggedInService]},
  {path: 'participants/statistics', component: ParticipantStatisticsComponent, canActivate: [LoggedInService]},
  {path: 'participants/fights', component: ParticipantFightListComponent, canActivate: [LoggedInService]},
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
