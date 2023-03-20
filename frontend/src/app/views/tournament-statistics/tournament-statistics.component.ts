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
import {DatePipe} from "@angular/common";
import {UserSessionService} from "../../services/user-session.service";
import {RoleType} from "../../models/role-type";
import {ScoreOfCompetitor} from "../../models/score-of-competitor";
import {RankingService} from "../../services/ranking.service";
import {NameUtilsService} from "../../services/name-utils.service";
import {ScoreOfTeam} from "../../models/score-of-team";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-tournament-statistics',
  templateUrl: './tournament-statistics.component.html',
  styleUrls: ['./tournament-statistics.component.scss']
})
export class TournamentStatisticsComponent extends RbacBasedComponent implements OnInit, OnDestroy {

  pipe: DatePipe;

  public scoreTypeChartData: PieChartData;

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
  public fightsOverData: GaugeChartData;

  private readonly tournamentId: number | undefined;
  public tournamentStatistics: TournamentStatistics | undefined = undefined;
  public roleTypes: RoleType[] = RoleType.toArray();
  competitorsScore: ScoreOfCompetitor[];
  teamScores: ScoreOfTeam[];


  constructor(private router: Router, rbacService: RbacService, private systemOverloadService: SystemOverloadService,
              private statisticsService: StatisticsService, private userSessionService: UserSessionService,
              private rankingService: RankingService, private nameUtilsService: NameUtilsService,
              private translateService: TranslateService) {
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
    this.setLocale();
  }

  private setLocale() {
    if (this.userSessionService.getLanguage() === 'es' || this.userSessionService.getLanguage() === 'ca') {
      this.pipe = new DatePipe('es');
    } else if (this.userSessionService.getLanguage() === 'it') {
      this.pipe = new DatePipe('it');
    } else if (this.userSessionService.getLanguage() === 'de') {
      this.pipe = new DatePipe('de');
    } else if (this.userSessionService.getLanguage() === 'nl') {
      this.pipe = new DatePipe('nl');
    } else {
      this.pipe = new DatePipe('en-US');
    }
  }


  ngOnInit(): void {
    this.generateStatistics();
    this.generateCompetitorsRanking();
  }

  generateStatistics(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.statisticsService.getTournamentStatistics(this.tournamentId!).subscribe((tournamentStatistics: TournamentStatistics) => {
      this.tournamentStatistics = TournamentStatistics.clone(tournamentStatistics);
      this.initializeScoreStatistics(tournamentStatistics);
      this.initializeFightsOverStatistics(tournamentStatistics);
      if (tournamentStatistics.teamSize > 1) {
        this.generateTeamsRanking();
      }
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
  }

  generateCompetitorsRanking(): void {
    if (this.tournamentId) {
      this.rankingService.getCompetitorsScoreRankingByTournament(this.tournamentId).subscribe(competitorsScore => {
        this.competitorsScore = competitorsScore;
      });
    }
  }

  getCompetitorRanking(scoreOfCompetitor: ScoreOfCompetitor): string {
    if (scoreOfCompetitor && scoreOfCompetitor.competitor) {
      return this.nameUtilsService.getDisplayName(scoreOfCompetitor.competitor, 1800);
    }
    return "";
  }

  generateTeamsRanking(): void {
    if (this.tournamentId) {
      this.rankingService.getTeamsScoreRankingByTournament(this.tournamentId).subscribe(scoresOfTeams => {
        this.teamScores = scoresOfTeams;
      });
    }
  }

  getTeamsRanking(scoreOfTeam: ScoreOfTeam): string {
    if (scoreOfTeam && scoreOfTeam.team) {
      return scoreOfTeam.team.name;
    }
    return "";
  }

  goBackToTournament(): void {
    this.router.navigate(['/tournaments'], {});
  }

  initializeScoreStatistics(tournamentStatistics: TournamentStatistics): void {
    const scores: [string, number][] = [];
    if (tournamentStatistics.fightStatistics.menNumber) {
      scores.push([Score.MEN, tournamentStatistics.fightStatistics.menNumber]);
    }
    if (tournamentStatistics.fightStatistics.koteNumber) {
      scores.push([Score.KOTE, tournamentStatistics.fightStatistics.koteNumber]);
    }
    if (tournamentStatistics.fightStatistics.doNumber) {
      scores.push([Score.DO, tournamentStatistics.fightStatistics.doNumber]);
    }
    if (tournamentStatistics.fightStatistics.tsukiNumber) {
      scores.push([Score.TSUKI, tournamentStatistics.fightStatistics.tsukiNumber]);
    }
    if (tournamentStatistics.fightStatistics.ipponNumber) {
      scores.push([Score.IPPON, tournamentStatistics.fightStatistics.ipponNumber]);
    }
    this.scoreTypeChartData = PieChartData.fromArray(scores);
  }

  initializeFightsOverStatistics(tournamentStatistics: TournamentStatistics): void {
    this.fightsOverData = GaugeChartData.fromArray([[this.translateService.instant('fightsFinished'),
      (tournamentStatistics.fightStatistics.fightsFinished / tournamentStatistics.fightStatistics.fightsNumber) ]]);
  }

  convertSeconds(seconds: number | undefined): string {
    if (seconds) {
      const minutes = Math.floor(seconds / 60);
      if (minutes > 0) {
        return minutes + "m " + seconds % 60 + "s";
      }
      return seconds + "s";
    }
    return "";
  }

  convertDate(date: Date | undefined): string | null {
    if (date) {
      return this.pipe.transform(date, 'short');
    }
    return "";
  }

  numberOfParticipantsByRole(roleType: RoleType): number {
    if (this.tournamentStatistics !== undefined) {
      return this.tournamentStatistics.numberOfParticipantsByRole(roleType);
    }
    return 0;
  }


}
