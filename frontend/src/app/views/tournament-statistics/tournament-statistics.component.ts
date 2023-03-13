import {Component, OnDestroy, OnInit} from '@angular/core';
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Router} from "@angular/router";
import {RbacService} from "../../services/rbac/rbac.service";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {BarChartData} from "../../components/charts/bar-chart/bar-chart-data";
import {Score} from "../../models/score";
import {StackedBarsChartData} from "../../components/charts/stacked-bars-chart/stacked-bars-chart-data";

@Component({
  selector: 'app-tournament-statistics',
  templateUrl: './tournament-statistics.component.html',
  styleUrls: ['./tournament-statistics.component.scss']
})
export class TournamentStatisticsComponent extends RbacBasedComponent implements OnInit, OnDestroy {

  constructor(private router: Router, rbacService: RbacService, private systemOverloadService: SystemOverloadService) {
    super(rbacService);
  }

  public scores: BarChartData[] =
    [
      new BarChartData(Score.MEN, 5),
      new BarChartData(Score.KOTE, 5),
      new BarChartData(Score.DO, 3)
    ];

  public scoresStacked: StackedBarsChartData[] =
    [
      new StackedBarsChartData(Score.MEN, ["A", "B", "C", "D"], [5, 4, 3, 2]),
      new StackedBarsChartData(Score.KOTE, ["A", "B", "C", "D"], [1, 2, 3, 4]),
      new StackedBarsChartData(Score.DO, ["A", "B", "C", "D"], [6, 7, 8, 9])
    ];

  ngOnInit(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.systemOverloadService.isTransactionalBusy.next(false);
  }

  goBackToTournament(): void {
    this.router.navigate(['/tournaments'], {});
  }
}
