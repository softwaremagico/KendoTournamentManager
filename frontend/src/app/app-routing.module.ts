import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ClubListComponent} from "./club-list/club-list.component";
import {LoggedInService} from './guards/logged-in.service';
import {LoginComponent} from './login/login.component';
import {ParticipantListComponent} from "./participant-list/participant-list.component";

const routes: Routes = [
  { path: '', redirectTo: '/participants', pathMatch: 'full' },
  {path: 'login', component: LoginComponent},
  {path: 'clubs', component: ClubListComponent, canActivate: [LoggedInService]},
  {path: 'participants', component: ParticipantListComponent, canActivate: [LoggedInService]}
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
