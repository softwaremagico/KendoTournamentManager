import {Component, Inject, OnInit, Optional} from '@angular/core';
import {ScoreOfTeam} from "../../models/score-of-team";
import {RankingService} from "../../services/ranking.service";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../models/tournament";
import {Subject} from "rxjs";
import {TranslateService} from "@ngx-translate/core";
import {UndrawTeamsComponent} from "../../views/fight-list/undraw-teams/undraw-teams.component";
import {Team} from "../../models/team";
import {RbacBasedComponent} from "../RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {Group} from "../../models/group";
import {TournamentType} from "../../models/tournament-type";
import {Action} from "../../action";

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

  private destroy$: Subject<void> = new Subject<void>();
  _loading = false;

  constructor(public dialogRef: MatDialogRef<TeamRankingComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: {
                tournament: Tournament, group: Group, finished: boolean,
                showIndex: boolean | undefined,
              },
              private rankingService: RankingService, public translateService: TranslateService, public dialog: MatDialog,
              rbacService: RbacService) {
    super(rbacService);
    this.tournament = data.tournament;
    this.group = data.group;
    this.fightsFinished = data.finished;
  }

  ngOnInit(): void {
    if (this.tournament) {
      if (this.tournament.type == TournamentType.CHAMPIONSHIP) {
        if (this.group) {
          this.rankingService.getTeamsScoreRankingByGroup(this.group!.id!).subscribe((scoresOfTeams: ScoreOfTeam[]): void => {
            this.teamScores = scoresOfTeams;
          });
        }
      } else {
        if (this.tournament?.id) {
          this.rankingService.getTeamsScoreRankingByTournament(this.tournament.id).subscribe((scoresOfTeams: ScoreOfTeam[]): void => {
            this.teamScores = scoresOfTeams;
          });
        }
      }
    }
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
    this.dialogRef.close({action: Action.Cancel});
  }

  downloadPDF(): void {
    if (this.tournament) {
      if (this.tournament.type == TournamentType.CHAMPIONSHIP) {
        if (this.group) {
          this.rankingService.getTeamsScoreRankingByGroupAsPdf(this.group!.id!).subscribe((pdf: Blob): void => {
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
  }

  undrawTeams(index: number): void {
    const teams: Team[] = this.getDrawWinners(index);
    this.dialog.open(UndrawTeamsComponent, {
      disableClose: false,
      data: {tournament: this.tournament, groupId: this.group.id, teams: teams}
    });
    this.dialogRef.close({action: Action.Update});
  }
}
