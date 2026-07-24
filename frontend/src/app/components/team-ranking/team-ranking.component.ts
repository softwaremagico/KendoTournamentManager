import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {ScoreOfTeam} from "../../models/score-of-team";
import {RankingService} from "../../services/ranking.service";
import {Tournament} from "../../models/tournament";
import {forkJoin, Observable} from "rxjs";
import {TranslocoService} from '@jsverse/transloco';
import {Team} from "../../models/team";
import {RbacBasedComponent} from "../RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {Group} from "../../models/group";
import {TournamentType} from "../../models/tournament-type";
import {TournamentExtendedPropertiesService} from "../../services/tournament-extended-properties.service";
import {TournamentExtraPropertyKey} from "../../models/tournament-extra-property-key";
import {TournamentExtendedProperty} from "../../models/tournament-extended-property.model";
import {MessageService} from "../../services/message.service";
import {Router} from "@angular/router";
import {NameUtilsService} from "../../services/name-utils.service";
import {ScoreType} from "../../models/score-type";
import {Duel} from "../../models/duel";
import {SwissTieBreakRule} from "../../models/swiss-tie-break-rule";

@Component({
  standalone: false,
  selector: 'team-ranking',
  templateUrl: './team-ranking.component.html',
  styleUrls: ['./team-ranking.component.scss']
})
export class TeamRankingComponent extends RbacBasedComponent implements OnInit, OnChanges {

  teamScores: ScoreOfTeam[];
  @Input()
  tournament: Tournament;
  @Input()
  fightsFinished: boolean;
  @Input()
  group: Group | undefined | null;
  @Input()
  showIndex: boolean | undefined;
  @Input()
  showDrawWarningOnInit: boolean = false;
  @Output()
  closed: EventEmitter<Duel[]> = new EventEmitter<Duel[]>();
  existsDraws: boolean = false;
  private drawWarningShown: boolean = false;
  numberOfWinners: number;
  protected untieTeamsPopup: boolean = false;
  protected drawTeams: Team[] = [];
  protected readonly ScoreType = ScoreType;
  protected swissTieBreakRule: SwissTieBreakRule = SwissTieBreakRule.BUCHHOLZ;
  protected readonly SwissTieBreakRule = SwissTieBreakRule;

  constructor(private readonly rankingService: RankingService, public readonly translateService: TranslocoService,
              private readonly tournamentExtendedPropertiesService: TournamentExtendedPropertiesService, private readonly messageService: MessageService,
              public override readonly rbacService: RbacService, private readonly router: Router,
              protected readonly nameUtils: NameUtilsService) {
    super(rbacService);
  }

  ngOnInit(): void {
    if (this.tournament) {
      if (this.tournament.type == TournamentType.SWISS) {
        if (this.tournament.id) {
          const rankingRequest: Observable<ScoreOfTeam[]> = this.rankingService.getTeamsScoreRankingByTournament(this.tournament.id);
          const tieBreakRuleRequest: Observable<TournamentExtendedProperty> = this.tournamentExtendedPropertiesService
            .getByTournamentAndKey(this.tournament, TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE);

          forkJoin([rankingRequest, tieBreakRuleRequest]).subscribe(([_scoresOfTeams, _tieBreakRule]): void => {
            this.teamScores = _scoresOfTeams;
            this.numberOfWinners = 1;
            this.swissTieBreakRule = SwissTieBreakRule.getByKey(_tieBreakRule?.propertyValue)
              ?? SwissTieBreakRule.BUCHHOLZ;
            this.updateDrawStatusAndWarning();
          });
        }
      } else if (this.tournament.type == TournamentType.CHAMPIONSHIP) {
        if (this.group) {
          const rankingRequest: Observable<ScoreOfTeam[]> = this.rankingService.getTeamsScoreRankingByGroup(this.group.id!);
          const winnersRequest: Observable<TournamentExtendedProperty> = this.tournamentExtendedPropertiesService.getByTournamentAndKey(this.tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);

          forkJoin([rankingRequest, winnersRequest]).subscribe(([_scoresOfTeams, _numberOfWinners]): void => {
            this.teamScores = _scoresOfTeams;
            this.numberOfWinners = _numberOfWinners ? Number(_numberOfWinners.propertyValue) : 1;
            this.updateDrawStatusAndWarning();
          });
        }
      } else {
        if (this.tournament?.id) {
          this.rankingService.getTeamsScoreRankingByTournament(this.tournament.id).subscribe((scoresOfTeams: ScoreOfTeam[]): void => {
            this.numberOfWinners = 1;
            this.teamScores = scoresOfTeams;
            this.updateDrawStatusAndWarning();
          });
        }
      }
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['showDrawWarningOnInit'] || changes['fightsFinished']) && this.teamScores) {
      this.updateDrawStatusAndWarning();
    }
  }

  importantDrawWinner(): boolean {
    if (this.tournament?.type === TournamentType.SWISS) {
      return this.isDrawWinner(0);
    }
    for (let i = 0; i < this.numberOfWinners; i++) {
      if (this.isDrawWinner(i)) {
        return true;
      }
    }
    return false;
  }

  isDrawWinner(index: number): boolean {
    if (this.tournament?.type === TournamentType.SWISS) {
      return index === 0 && this.fightsFinished && this.getSwissChampionDrawTeams().length > 1;
    }
    return this.teamScores && this.fightsFinished && this.teamScores.filter((scoreOfTeam: ScoreOfTeam): boolean => scoreOfTeam.sortingIndex === index).length > 1;
  }

  getDrawWinners(index: number): Team[] {
    if (this.tournament?.type === TournamentType.SWISS) {
      return index === 0 ? this.getSwissChampionDrawTeams() : [];
    }
    const teams: Team[] = [];
    if (this.teamScores && this.fightsFinished) {
      const scores: ScoreOfTeam[] = this.teamScores.filter((scoreOfTeam: ScoreOfTeam): boolean => scoreOfTeam.sortingIndex === index);
      for (const scoreOfTeam of scores) {
        teams.push(scoreOfTeam.team);
      }
    }
    return teams;
  }

  closeDialog(): void {
    this.closed.emit();
  }

  downloadPDF(): void {
    if (this.tournament) {
      if (this.tournament.type == TournamentType.CHAMPIONSHIP && this.group) {
        this.rankingService.getTeamsScoreRankingByGroupAsPdf(this.group.id!).subscribe((pdf: Blob): void => {
          const blob: Blob = new Blob([pdf], {type: 'application/pdf'});
          const downloadURL: string = window.URL.createObjectURL(blob);
          const anchor: HTMLAnchorElement = document.createElement("a");
          if (this.group) {
            anchor.download = `Team Ranking - ${this.tournament.name} (group ${this.group.index + 1}).pdf`;
          } else {
            anchor.download = `Team Ranking - ${this.tournament.name}.pdf`;
          }
          anchor.href = downloadURL;
          anchor.click();
        });
      } else {
        if (this.tournament?.id) {
          this.rankingService.getTeamsScoreRankingByTournamentAsPdf(this.tournament.id).subscribe((pdf: Blob): void => {
            const blob: Blob = new Blob([pdf], {type: 'application/pdf'});
            const downloadURL: string = window.URL.createObjectURL(blob);
            const anchor: HTMLAnchorElement = document.createElement("a");
            anchor.download = "Team Ranking - " + this.tournament.name + ".pdf";
            anchor.href = downloadURL;
            anchor.click();
          });
        }
      }
    }
  }

  untieAllTeams(): void {
    for (let i = 0; i < this.numberOfWinners; i++) {
      if (this.isDrawWinner(i)) {
        this.untieTeams(i);
      }
    }
  }

  untieTeams(index: number): void {
    this.drawTeams = this.getDrawWinners(index);
    this.untieTeamsPopup = true;
  }

  untieFights(duels: Duel[]) {
    this.untieTeamsPopup = false;
    if (duels && duels.length > 0) {
      this.closed.emit(duels);
    }
  }

  openStatistics(): void {
    if (this.tournament) {
      this.closeDialog();
      this.router.navigate(['/tournaments/statistics'], {state: {tournamentId: this.tournament.id}});
    }
  }

  getTeamMembers(team: Team): string {
    let teamMembers: string = "";
    for (const member of team.members) {
      teamMembers += this.nameUtils.getNameLastname(member) + "\n";
    }
    return teamMembers;
  }

  protected showSwissTieBreakScore(): boolean {
    return this.tournament?.type === TournamentType.SWISS && [SwissTieBreakRule.BUCHHOLZ,
      SwissTieBreakRule.MEDIAN_BUCHHOLZ, SwissTieBreakRule.SONNEBORN_BERGER].includes(this.swissTieBreakRule);
  }

  protected getSwissTieBreakHeaderTranslationKey(): string {
    return SwissTieBreakRule.toCamel(this.swissTieBreakRule);
  }

  protected getSwissTieBreakValue(score: ScoreOfTeam): string {
    if (score.swissTieBreakValue === null || score.swissTieBreakValue === undefined) {
      return '-';
    }
    const ruleUsed = SwissTieBreakRule.getByKey(score.swissTieBreakRuleUsed || '')
      ?? this.swissTieBreakRule;
    if (ruleUsed === SwissTieBreakRule.SONNEBORN_BERGER) {
      return score.swissTieBreakValue.toFixed(1);
    }
    return score.swissTieBreakValue.toFixed(0);
  }

  private getSwissChampionDrawTeams(): Team[] {
    if (!this.teamScores || this.teamScores.length === 0) {
      return [];
    }
    const maxPoints = Math.max(...this.teamScores.map((score: ScoreOfTeam): number => this.getSwissMatchPoints(score)));
    return this.teamScores
      .filter((score: ScoreOfTeam): boolean => this.getSwissMatchPoints(score) === maxPoints)
      .map((score: ScoreOfTeam): Team => score.team);
  }

  private getSwissMatchPoints(score: ScoreOfTeam): number {
    const winPoints: number = this.tournament?.tournamentScore?.pointsByVictory ?? 3;
    const drawPoints: number = this.tournament?.tournamentScore?.pointsByDraw ?? 1;
    const wonFights: number = score.wonFights ?? 0;
    const drawFights: number = score.drawFights ?? 0;
    return wonFights * winPoints + drawFights * drawPoints;
  }

  private updateDrawStatusAndWarning(): void {
    this.existsDraws = this.importantDrawWinner();
    if (this.hasRelevantWinnerDraw() && this.showDrawWarningOnInit && !this.drawWarningShown) {
      this.messageService.warningMessage(this.getDrawWarningMessageKey());
      this.drawWarningShown = true;
    }
  }

  private getDrawWarningMessageKey(): string {
    if (this.tournament?.type === TournamentType.CHAMPIONSHIP) {
      return "drawScore";
    }
    return "drawTopPositionsScore";
  }

  private hasRelevantWinnerDraw(): boolean {
    if (!this.teamScores || this.teamScores.length === 0) {
      return false;
    }
    if (this.tournament?.type === TournamentType.SWISS) {
      return this.getSwissChampionDrawTeams().length > 1;
    }
    for (let i = 0; i < this.numberOfWinners; i++) {
      if (this.teamScores.filter((scoreOfTeam: ScoreOfTeam): boolean => scoreOfTeam.sortingIndex === i).length > 1) {
        return true;
      }
    }
    return false;
  }

}
