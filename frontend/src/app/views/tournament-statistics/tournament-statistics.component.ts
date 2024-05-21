import {AfterViewInit, Component, OnDestroy, ViewChild} from '@angular/core';
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Router} from "@angular/router";
import {RbacService} from "../../services/rbac/rbac.service";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {PieChartData} from "../../components/charts/pie-chart/pie-chart-data";
import {Score} from "../../models/score";
import {LineChartData, LineChartDataElement} from "../../components/charts/line-chart/line-chart-data";
import {
  StackedBarChartData,
  StackedBarChartDataElement
} from "../../components/charts/stacked-bars-chart/stacked-bars-chart-data";
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
import {LineChartComponent} from "../../components/charts/line-chart/line-chart.component";
import {StackedBarsChartComponent} from "../../components/charts/stacked-bars-chart/stacked-bars-chart.component";
import {truncate} from "../../utils/maths/truncate";
import {convertDate, convertSeconds} from "../../utils/dates/date-conversor";
import {Achievement} from "../../models/achievement.model";
import {AchievementsService} from "../../services/achievements.service";
import {Tournament} from "../../models/tournament";
import {TournamentService} from "../../services/tournament.service";
import {environment} from "../../../environments/environment";

@Component({
  selector: 'app-tournament-statistics',
  templateUrl: './tournament-statistics.component.html',
  styleUrls: ['./tournament-statistics.component.scss']
})
export class TournamentStatisticsComponent extends RbacBasedComponent implements OnDestroy, AfterViewInit {

  pipe: DatePipe;

  public scoreTypeChartData: PieChartData;

  public timeByTournament: LineChartData = new LineChartData();
  public teamSizeByTournament: LineChartData = new LineChartData();
  public participantsByTournament: StackedBarChartData = new StackedBarChartData();
  public hitsByTournament: StackedBarChartData = new StackedBarChartData();
  public fightsOverData: GaugeChartData;

  protected achievementsEnabled: boolean = JSON.parse(environment.achievementsEnabled);

  private readonly tournamentId: number | undefined;
  public tournamentStatistics: TournamentStatistics | undefined = undefined;
  public roleTypes: RoleType[] = RoleType.toArray();
  competitorsScore: ScoreOfCompetitor[];
  teamScores: ScoreOfTeam[];

  public achievements: Achievement[];

  @ViewChild('timeByTournamentChart')
  timeByTournamentChart: LineChartComponent;

  @ViewChild('participantsByTournamentChart')
  participantsByTournamentChart: StackedBarsChartComponent;

  @ViewChild('hitsByTournamentChart')
  hitsByTournamentChart: StackedBarsChartComponent;

  @ViewChild('timeSizeByTournamentChart')
  timeSizeByTournamentChart: LineChartComponent;

  public tournament: Tournament;


  constructor(private router: Router, rbacService: RbacService, private systemOverloadService: SystemOverloadService,
              private statisticsService: StatisticsService, private userSessionService: UserSessionService,
              private rankingService: RankingService, private nameUtilsService: NameUtilsService,
              private translateService: TranslateService, private achievementService: AchievementsService,
              private tournamentService: TournamentService) {
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

  private setLocale(): void {
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

  ngAfterViewInit(): void {
    if (this.tournamentId) {
      this.tournamentService.get(this.tournamentId).subscribe((_tournament: Tournament): void => {
        this.tournament = _tournament;
      })
    }
    this.generateStatistics();
    this.generateCompetitorsRanking();
  }


  generateStatistics(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.statisticsService.getTournamentStatistics(this.tournamentId!).subscribe((tournamentStatistics: TournamentStatistics) => {
      this.tournamentStatistics = TournamentStatistics.clone(tournamentStatistics);
      this.initializeScoreStatistics(this.tournamentStatistics);
      this.initializeFightsOverStatistics(this.tournamentStatistics);
      this.generateStackedStatistics(this.tournamentStatistics);
      this.generatePreviousTournamentsStatistics(this.tournamentStatistics.tournamentId);
      if (tournamentStatistics?.teamSize > 1) {
        this.generateTeamsRanking();
      }
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
    if (this.achievementsEnabled) {
      this.achievementService.getTournamentAchievements(this.tournamentId!).subscribe((_achievements: Achievement[]): void => {
        this.achievements = _achievements;
      });
    }
  }

  generatePreviousTournamentsStatistics(tournamentId: number): void {
    if (tournamentId) {
      this.statisticsService.getPreviousTournamentStatistics(tournamentId, 5)
        .subscribe((tournamentStatistics: TournamentStatistics[]): void => {
          if (tournamentStatistics) {
            for (let tournamentStatistic of tournamentStatistics) {
              if (tournamentStatistic && tournamentStatistic.tournamentId != tournamentId) {
                this.generateStackedStatistics(TournamentStatistics.clone(tournamentStatistic));
              }
            }
          }
        });
    }
  }

  generateStackedStatistics(tournamentStatistics: TournamentStatistics): void {
    this.generateParticipantsByTournamentStatistics(tournamentStatistics);
    this.generateTeamSizeByTournamentStatistics(tournamentStatistics);
    this.generateHitsByTournamentStatistics(tournamentStatistics);
    this.generateTimeByTournamentStatistics(tournamentStatistics);
  }

  generateParticipantsByTournamentStatistics(tournamentStatistics: TournamentStatistics): void {
    this.participantsByTournament.elements.unshift(new StackedBarChartDataElement(this.obtainParticipants(tournamentStatistics), this.getLabel(tournamentStatistics.tournamentName)));
    this.participantsByTournamentChart.update(this.participantsByTournament);
  }

  generateTeamSizeByTournamentStatistics(tournamentStatistics: TournamentStatistics): void {
    //Team Size
    if (!this.teamSizeByTournament.elements[0]) {
      this.teamSizeByTournament.elements[0] = new LineChartDataElement();
      this.teamSizeByTournament.elements[0].name = this.translateService.instant('teamSize');
    }
    this.teamSizeByTournament.elements[0].points.unshift(this.obtainTeamSize(tournamentStatistics));

    //Total Teams
    if (!this.teamSizeByTournament.elements[1]) {
      this.teamSizeByTournament.elements[1] = new LineChartDataElement();
      this.teamSizeByTournament.elements[1].name = this.translateService.instant('teams');
    }
    this.teamSizeByTournament.elements[1].points.unshift(this.obtainTeams(tournamentStatistics));

    this.timeSizeByTournamentChart.update(this.teamSizeByTournament);
  }

  generateHitsByTournamentStatistics(tournamentStatistics: TournamentStatistics): void {
    this.hitsByTournament.elements.unshift(new StackedBarChartDataElement(this.obtainPoints(tournamentStatistics), this.getLabel(tournamentStatistics.tournamentName)));
    this.hitsByTournamentChart.update(this.hitsByTournament);
  }

  generateTimeByTournamentStatistics(tournamentStatistics: TournamentStatistics): void {
    //Fight time
    if (!this.timeByTournament.elements[0]) {
      this.timeByTournament.elements[0] = new LineChartDataElement();
      this.timeByTournament.elements[0].name = this.translateService.instant('fightsDuration');
    }
    this.timeByTournament.elements[0].points.unshift(this.obtainTimes(tournamentStatistics));

    //Tournament time
    if (!this.timeByTournament.elements[1]) {
      this.timeByTournament.elements[1] = new LineChartDataElement();
      this.timeByTournament.elements[1].name = this.translateService.instant('tournamentActiveTime');
    }
    this.timeByTournament.elements[1].points.unshift(this.obtainTournamentTimes(tournamentStatistics));


    this.timeByTournamentChart.update(this.timeByTournament);
  }

  generateCompetitorsRanking(): void {
    if (this.tournamentId) {
      this.rankingService.getCompetitorsScoreRankingByTournament(this.tournamentId).subscribe((competitorsScore: ScoreOfCompetitor[]): void => {
        this.competitorsScore = competitorsScore;
      });
    }
  }

  getCompetitorRanking(scoreOfCompetitor: ScoreOfCompetitor): string {
    if (scoreOfCompetitor?.competitor) {
      return this.nameUtilsService.getDisplayName(scoreOfCompetitor.competitor, 1800);
    }
    return "";
  }

  generateTeamsRanking(): void {
    if (this.tournamentId) {
      this.rankingService.getTeamsScoreRankingByTournament(this.tournamentId).subscribe((scoresOfTeams: ScoreOfTeam[]): void => {
        this.teamScores = scoresOfTeams;
      });
    }
  }

  getTeamsRanking(scoreOfTeam: ScoreOfTeam): string {
    if (scoreOfTeam?.team) {
      return scoreOfTeam.team.name;
    }
    return "";
  }

  goBackToTournament(): void {
    this.router.navigate(['/tournaments'], {});
  }

  initializeScoreStatistics(tournamentStatistics: TournamentStatistics): void {
    this.scoreTypeChartData = PieChartData.fromArray(this.obtainPoints(tournamentStatistics));
  }

  obtainParticipants(tournamentStatistics: TournamentStatistics): [string, number][] {
    const participants: [string, number][] = [];
    if (tournamentStatistics) {
      participants.push([this.translateService.instant(RoleType.COMPETITOR.toLowerCase()), tournamentStatistics.numberOfParticipantsByRole(RoleType.COMPETITOR)]);
      participants.push([this.translateService.instant(RoleType.REFEREE.toLowerCase()), tournamentStatistics.numberOfParticipantsByRole(RoleType.REFEREE)]);
      participants.push([this.translateService.instant(RoleType.ORGANIZER.toLowerCase()), tournamentStatistics.numberOfParticipantsByRole(RoleType.ORGANIZER)]);
      participants.push([this.translateService.instant(RoleType.VOLUNTEER.toLowerCase()), tournamentStatistics.numberOfParticipantsByRole(RoleType.VOLUNTEER)]);
      participants.push([this.translateService.instant(RoleType.PRESS.toLowerCase()), tournamentStatistics.numberOfParticipantsByRole(RoleType.PRESS)]);
    }
    return participants;
  }

  obtainPoints(tournamentStatistics: TournamentStatistics): [string, number][] {
    const scores: [string, number][] = [];
    if (tournamentStatistics?.tournamentFightStatistics) {
      scores.push([this.translateService.instant(Score.toCamel(Score.MEN)), tournamentStatistics.tournamentFightStatistics.menNumber ? tournamentStatistics.tournamentFightStatistics.menNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.KOTE)), tournamentStatistics.tournamentFightStatistics.koteNumber ? tournamentStatistics.tournamentFightStatistics.koteNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.DO)), tournamentStatistics.tournamentFightStatistics.doNumber ? tournamentStatistics.tournamentFightStatistics.doNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.TSUKI)), tournamentStatistics.tournamentFightStatistics.tsukiNumber ? tournamentStatistics.tournamentFightStatistics.tsukiNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.IPPON)), tournamentStatistics.tournamentFightStatistics.ipponNumber ? tournamentStatistics.tournamentFightStatistics.ipponNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.FUSEN_GACHI)), tournamentStatistics.tournamentFightStatistics.fusenGachiNumber ? tournamentStatistics.tournamentFightStatistics.fusenGachiNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.HANSOKU)), tournamentStatistics.tournamentFightStatistics.hansokuNumber ? tournamentStatistics.tournamentFightStatistics.hansokuNumber : 0]);
    }
    return scores;
  }

  obtainTimes(tournamentStatistics: TournamentStatistics): [string, number] {
    let times: [string, number];
    if (tournamentStatistics.tournamentFightStatistics?.fightsFinishedAt && tournamentStatistics.tournamentFightStatistics?.fightsStartedAt &&
      tournamentStatistics.tournamentFightStatistics?.fightsFinishedAt && tournamentStatistics.tournamentFightStatistics?.fightsStartedAt) {
      //Time in minutes.
      times = [this.getLabel(tournamentStatistics.tournamentName), Math.max(0, truncate((new Date(tournamentStatistics.tournamentFightStatistics.fightsFinishedAt).getTime() -
        new Date(tournamentStatistics.tournamentFightStatistics.fightsStartedAt).getTime()) / (1000 * 60), 2))];
    } else {
      times = [this.getLabel(tournamentStatistics.tournamentName), 0];
    }
    return times;
  }

  obtainTeamSize(tournamentStatistics: TournamentStatistics): [string, number] {
    let sizes: [string, number];
    if (tournamentStatistics.teamSize) {
      //Time in minutes.
      sizes = [this.getLabel(tournamentStatistics.tournamentName), tournamentStatistics.teamSize];
    } else {
      sizes = [this.getLabel(tournamentStatistics.tournamentName), 0];
    }
    return sizes;
  }

  obtainTeams(tournamentStatistics: TournamentStatistics): [string, number] {
    let sizes: [string, number];
    if (tournamentStatistics.teamSize) {
      //Time in minutes.
      sizes = [this.getLabel(tournamentStatistics.tournamentName), tournamentStatistics.numberOfTeams];
    } else {
      sizes = [this.getLabel(tournamentStatistics.tournamentName), 0];
    }
    return sizes;
  }

  obtainTournamentTimes(tournamentStatistics: TournamentStatistics): [string, number] {
    let times: [string, number];
    if (tournamentStatistics.tournamentFinishedAt && tournamentStatistics.tournamentCreatedAt) {
      //Time in minutes.
      times = [this.getLabel(tournamentStatistics.tournamentName), Math.max(0, truncate((new Date(tournamentStatistics.tournamentFinishedAt).getTime() -
        new Date(tournamentStatistics.tournamentCreatedAt).getTime()) / (1000 * 60), 2))];
    } else {
      times = [this.getLabel(tournamentStatistics.tournamentName), 0];
    }
    return times;
  }

  initializeFightsOverStatistics(tournamentStatistics: TournamentStatistics): void {
    const progress: number = (tournamentStatistics.tournamentFightStatistics?.fightsFinished / tournamentStatistics.tournamentFightStatistics?.fightsNumber) * 100;
    this.fightsOverData = GaugeChartData.fromArray([[this.translateService.instant('fightsFinished'),
      isNaN(progress) ? 0 : progress]]);
  }

  convertSeconds(seconds: number | undefined): string {
    return convertSeconds(seconds);
  }

  convertDate(date: Date | undefined): string | null {
    return convertDate(this.pipe, date);
  }

  numberOfParticipantsByRole(roleType: RoleType): number {
    if (this.tournamentStatistics !== undefined) {
      return this.tournamentStatistics.numberOfParticipantsByRole(roleType);
    }
    return 0;
  }

  getLabel(label: string): string {
    //I know, this is only for me
    label = label.replace("Liga Interna", "").trim();
    if (label.length > 15) {
      return label.split(/\s/)
        .reduce((accumulator, word) => accumulator + word.charAt(0) + ". ", '');
    }
    return label;
  }


  protected readonly undefined = undefined;
}
