import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ClubListComponent} from "./club-list/club-list.component";
import {LoggedInService} from './guards/logged-in.service';
import {LoginComponent} from './login/login.component';
import {UserListComponent} from "./user-list/user-list/user-list.component";

const routes: Routes = [
  { path: '', redirectTo: '/clubs', pathMatch: 'full' },
  {path: 'login', component: LoginComponent},
  {path: 'clubs', component: ClubListComponent, canActivate: [LoggedInService]},
  {path: 'participants', component: UserListComponent, canActivate: [LoggedInService]}
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
