import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {TournamentListComponent} from "./tournament-list.component";
import {FightListComponent} from "../fight-list/fight-list.component";
import {LoggedInService} from "../../interceptors/logged-in.service";
import {TournamentStatisticsComponent} from "../tournament-statistics/tournament-statistics.component";

const routes: Routes = [
  {path: '', component: TournamentListComponent},
  {path: 'fights', component: FightListComponent, canActivate: [LoggedInService]},
  {path: 'statistics', component: TournamentStatisticsComponent, canActivate: [LoggedInService]},
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentRoutingModule {
}