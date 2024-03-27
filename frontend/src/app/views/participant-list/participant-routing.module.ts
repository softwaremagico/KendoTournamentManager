import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {ParticipantListComponent} from "./participant-list.component";
import {ParticipantStatisticsComponent} from "../participant-statistics/participant-statistics.component";
import {LoggedInService} from "../../interceptors/logged-in.service";

const routes: Routes = [
  {path: '', component: ParticipantListComponent, canActivate: [LoggedInService]},
  {path: 'statistics', component: ParticipantStatisticsComponent},
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ParticipantRoutingModule {
}
