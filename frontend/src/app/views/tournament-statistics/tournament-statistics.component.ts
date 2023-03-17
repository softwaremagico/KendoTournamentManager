import {Component, OnDestroy, OnInit} from '@angular/core';
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Router} from "@angular/router";
import {RbacService} from "../../services/rbac/rbac.service";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {PieChartData} from "../../components/charts/pie-chart/pie-chart-data";
import {Score} from "../../models/score";
import {BarChartData} from "../../components/charts/bar-chart/bar-chart-data";
import {LineChartData, LineChartDataElement} from "../../components/charts/line-chart/line-chart-data";
import {
  StackedBarChartData,
  StackedBarChartDataElement
} from "../../components/charts/stacked-bars-chart/stacked-bars-chart-data";
import {RadarChartData, RadarChartDataElement} from "../../components/charts/radar-chart/radar-chart-data";
import {RadialChartData} from "../../components/charts/radial-chart/radial-chart-data";
import {GaugeChartData} from "../../components/charts/gauge-chart/gauge-chart-data";
import {StatisticsService} from "../../services/statistics.service";
import {TournamentStatistics} from "../../models/tournament-statistics.model";

@Component({
  selector: 'app-tournament-statistics',
  templateUrl: './tournament-statistics.component.html',
  styleUrls: ['./tournament-statistics.component.scss']
})
export class TournamentStatisticsComponent extends RbacBasedComponent implements OnInit, OnDestroy {

  public pieChartData: PieChartData = PieChartData.fromArray([[Score.MEN, 5], [Score.DO, 4], [Score.KOTE, 1]]);
  public barChartData: BarChartData = BarChartData.fromArray([[Score.MEN, 5], [Score.DO, 4], [Score.KOTE, 1]]);
  public lineChartData: LineChartData = LineChartData.fromArray([[Score.MEN, 5], [Score.DO, 4], [Score.KOTE, 1]]);
  public multipleLineChartData: LineChartData = LineChartData.fromMultipleDataElements([
    new LineChartDataElement([[Score.MEN, 5], [Score.DO, 4], [Score.KOTE, 1]], "Tournament1"),
    new LineChartDataElement([[Score.MEN, 1], [Score.DO, 2], [Score.KOTE, 3]], "Tournament2")]);
  public multipleBarsChartData: StackedBarChartData = StackedBarChartData.fromMultipleDataElements([
    new StackedBarChartDataElement([[Score.MEN, 5], [Score.DO, 4], [Score.KOTE, 1]], "Tournament1"),
    new StackedBarChartDataElement([[Score.MEN, 1], [Score.DO, 2], [Score.KOTE, 3]], "Tournament2")]);
  public radarBarsChartData: RadarChartData = RadarChartData.fromMultipleDataElements([
    new RadarChartDataElement([[Score.MEN, 5], [Score.DO, 4], [Score.KOTE, 1]], "Tournament1"),
    new RadarChartDataElement([[Score.MEN, 1], [Score.DO, 2], [Score.KOTE, 3]], "Tournament2"),
    new RadarChartDataElement([[Score.MEN, 4], [Score.DO, 3], [Score.KOTE, 3]], "Tournament3"),
    new RadarChartDataElement([[Score.MEN, 1], [Score.DO, 2], [Score.KOTE, 3]], "Tournament4"),
    new RadarChartDataElement([[Score.MEN, 6], [Score.DO, 2], [Score.KOTE, 3]], "Tournament5")]);
  public radialChartData: RadialChartData = RadialChartData.fromArray([[Score.MEN, 85], [Score.DO, 49], [Score.KOTE, 36]]);
  public gaugeChartData: GaugeChartData = GaugeChartData.fromArray([[Score.MEN, 85]]);

  private readonly tournamentId: number | undefined;


  constructor(private router: Router, rbacService: RbacService, private systemOverloadService: SystemOverloadService,
              private statisticsService: StatisticsService) {
    super(rbacService);
    let state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      if (state['tournamentId'] && !isNaN(Number(state['tournamentId']))) {
        this.tournamentId = Number(state['tournamentId']);
      } else {
        this.goBackToTournament();
      }
    } else {
      this.goBackToTournament();
    }
  }


  ngOnInit(): void {
    this.generateStatistics();
  }

  generateStatistics() {
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.statisticsService.getTournamentStatistics(this.tournamentId!).subscribe((tournamentStatistics: TournamentStatistics) => {
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
  }

  goBackToTournament(): void {
    this.router.navigate(['/tournaments'], {});
  }
}
