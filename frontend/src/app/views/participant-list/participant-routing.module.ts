import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {ParticipantListComponent} from "./participant-list.component";
import {ParticipantStatisticsComponent} from "../participant-statistics/participant-statistics.component";
import {LoggedInService} from "../../interceptors/logged-in.service";

const routes: Routes = [
  {path: '', component: ParticipantListComponent},
  {path: 'statistics', component: ParticipantStatisticsComponent, canActivate: [LoggedInService]},
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ParticipantRoutingModule {
}
