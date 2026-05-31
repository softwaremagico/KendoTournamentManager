import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {ParticipantListComponent} from "./participant-list.component";
import {ParticipantStatisticsComponent} from "../participant-statistics/participant-statistics.component";
import {LoggedInService} from "../../interceptors/logged-in.service";
import {ParticipantFightListComponent} from "../participant-fight-list/participant-fight-list.component";

const routes: Routes = [
  {path: '', component: ParticipantListComponent, canActivate: [LoggedInService]},
  {path: 'statistics', component: ParticipantStatisticsComponent},
  {path: 'fights', component: ParticipantFightListComponent},
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ParticipantRoutingModule {
}
