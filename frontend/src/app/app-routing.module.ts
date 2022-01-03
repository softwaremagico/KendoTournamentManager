import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ClubListComponent} from "./club-list/club-list.component";

const routes: Routes = [
  // { path: '', redirectTo: '/clubs', pathMatch: 'full' },
  { path: 'clubs', component: ClubListComponent }
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
