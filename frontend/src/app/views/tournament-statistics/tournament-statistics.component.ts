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


  public scores: BarChartData[] =
    [
      new BarChartData(Score.MEN, 5),
      new BarChartData(Score.KOTE, 5),
      new BarChartData(Score.DO, 3)
    ];


  keys: string[] = ["Tournament1", "Tournament2", "Tournament3", "Tournament4"];
  values: Map<any, Map<any, any>> = new Map();


  public scoresStacked: StackedBarsChartData;

  constructor(private router: Router, rbacService: RbacService, private systemOverloadService: SystemOverloadService) {
    super(rbacService);

    this.values = new Map();
    let i: number = 1;
    for (const key of this.keys) {
      this.values.set(key, new Map());
      for (const score of Score.getKeys()) {
        this.values.get(key)!.set(score, i);
        i++;
      }
    }
    this.scoresStacked = new StackedBarsChartData(this.values);
  }


  ngOnInit(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.systemOverloadService.isTransactionalBusy.next(false);
  }

  goBackToTournament(): void {
    this.router.navigate(['/tournaments'], {});
  }
}
