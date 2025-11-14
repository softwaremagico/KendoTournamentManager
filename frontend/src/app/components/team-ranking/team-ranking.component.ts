import {Component, Inject, OnInit, Optional} from '@angular/core';
import {ScoreOfTeam} from "../../models/score-of-team";
import {RankingService} from "../../services/ranking.service";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../models/tournament";
import {forkJoin, Observable} from "rxjs";
import {TranslateService} from "@ngx-translate/core";
import {UndrawTeamsComponent} from "../../views/fight-list/undraw-teams/undraw-teams.component";
import {Team} from "../../models/team";
import {RbacBasedComponent} from "../RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {Group} from "../../models/group";
import {TournamentType} from "../../models/tournament-type";
import {Action} from "../../action";
import {TournamentExtendedPropertiesService} from "../../services/tournament-extended-properties.service";
import {TournamentExtraPropertyKey} from "../../models/tournament-extra-property-key";
import {TournamentExtendedProperty} from "../../models/tournament-extended-property.model";
import {MessageService} from "../../services/message.service";
import {Router} from "@angular/router";
import {NameUtilsService} from "../../services/name-utils.service";

@Component({
  selector: 'app-team-ranking',
  templateUrl: './team-ranking.component.html',
  styleUrls: ['./team-ranking.component.scss']
})
export class TeamRankingComponent extends RbacBasedComponent implements OnInit {

  teamScores: ScoreOfTeam[];
  tournament: Tournament;
  fightsFinished: boolean;
  group: Group;
  showIndex: boolean | undefined;
  existsDraws: boolean = false;
  numberOfWinners: number;

  constructor(public dialogRef: MatDialogRef<TeamRankingComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: {
                tournament: Tournament, group: Group, finished: boolean,
                showIndex: boolean | undefined,
              },
              private rankingService: RankingService, public translateService: TranslateService, public dialog: MatDialog,
              private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService, private messageService: MessageService,
              public override rbacService: RbacService, private router: Router,
              private nameUtils: NameUtilsService) {
    super(rbacService);
    this.tournament = data.tournament;
    this.group = data.group;
    this.fightsFinished = data.finished;
  }

  ngOnInit(): void {
    if (this.tournament) {
      if (this.tournament.type == TournamentType.CHAMPIONSHIP) {
        if (this.group) {
          const rankingRequest: Observable<ScoreOfTeam[]> = this.rankingService.getTeamsScoreRankingByGroup(this.group.id!);
          const winnersRequest: Observable<TournamentExtendedProperty> = this.tournamentExtendedPropertiesService.getByTournamentAndKey(this.tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);

          forkJoin([rankingRequest, winnersRequest]).subscribe(([_scoresOfTeams, _numberOfWinners]): void => {
            this.teamScores = _scoresOfTeams;
            this.numberOfWinners = _numberOfWinners ? Number(_numberOfWinners.propertyValue) : 1;
            if (this.isDrawWinner(0) || (_numberOfWinners && _numberOfWinners.propertyValue == "2" && this.isDrawWinner(1))) {
              this.messageService.warningMessage("drawScore");
              this.existsDraws = true;
            }
          });
        }
      } else {
        if (this.tournament?.id) {
          this.rankingService.getTeamsScoreRankingByTournament(this.tournament.id).subscribe((scoresOfTeams: ScoreOfTeam[]): void => {
            this.numberOfWinners = 1;
            this.teamScores = scoresOfTeams;
          });
        }
      }
    }
  }

  importantDrawWinner(): boolean {
    for (let i = 0; i < this.numberOfWinners; i++) {
      if (this.isDrawWinner(i)) {
        return true;
      }
    }
    return false;
  }

  isDrawWinner(index: number): boolean {
    return this.teamScores && this.fightsFinished && this.teamScores.filter((scoreOfTeam: ScoreOfTeam): boolean => scoreOfTeam.sortingIndex === index).length > 1;
  }

  getDrawWinners(index: number): Team[] {
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
    this.dialogRef.close({action: Action.Cancel, draws: this.existsDraws});
  }

  downloadPDF(): void {
    if (this.tournament) {
      if (this.tournament.type == TournamentType.CHAMPIONSHIP && this.group) {
        this.rankingService.getTeamsScoreRankingByGroupAsPdf(this.group.id!).subscribe((pdf: Blob): void => {
          const blob: Blob = new Blob([pdf], {type: 'application/pdf'});
          const downloadURL: string = window.URL.createObjectURL(blob);
          const anchor: HTMLAnchorElement = document.createElement("a");
          anchor.download = `Team Ranking - ${this.tournament.name} (group ${this.group.index + 1}).pdf`;
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

  undrawAllTeams(): void {
    for (let i = 0; i < this.numberOfWinners; i++) {
      if (this.isDrawWinner(i)) {
        this.undrawTeams(i);
      }
    }
  }

  undrawTeams(index: number): void {
    const teams: Team[] = this.getDrawWinners(index);
    this.dialog.open(UndrawTeamsComponent, {
      panelClass: 'pop-up-panel',
      disableClose: false,
      data: {tournament: this.tournament, groupId: this.group.id, teams: teams}
    });
    this.dialogRef.afterClosed().subscribe(result => {
      if (result?.action === Action.Update) {
        this.dialogRef.close({action: Action.Update, draws: result.draws});
      }
    });
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
}
