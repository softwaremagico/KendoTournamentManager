import {Component, Inject, OnInit, Optional} from '@angular/core';
import {ScoreOfTeam} from "../../../models/score-of-team";
import {RankingService} from "../../../services/ranking.service";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../models/tournament";
import {concatMap, from, Subject, takeWhile} from "rxjs";
import {TranslateService} from "@ngx-translate/core";
import {UndrawTeamsComponent} from "../undraw-teams/undraw-teams.component";
import {Team} from "../../../models/team";

@Component({
  selector: 'app-team-ranking',
  templateUrl: './team-ranking.component.html',
  styleUrls: ['./team-ranking.component.scss']
})
export class TeamRankingComponent implements OnInit {

  teamScores: ScoreOfTeam[];
  tournament: Tournament;
  fightsFinished: boolean;

  private destroy$: Subject<void> = new Subject<void>();
  _loading = false;

  constructor(public dialogRef: MatDialogRef<TeamRankingComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament, finished: boolean },
              private rankingService: RankingService, public translateService: TranslateService, public dialog: MatDialog) {
    this.tournament = data.tournament;
    this.fightsFinished = data.finished;
  }

  ngOnInit(): void {
    if (this.tournament && this.tournament.id) {
      this.rankingService.getTeamsScoreRankingByTournament(this.tournament.id).subscribe(scoresOfTeams => {
        this.teamScores = scoresOfTeams;
      });
    }
  }

  isDrawWinner(index: number): boolean {
    return this.teamScores && this.fightsFinished && this.teamScores.filter((scoreOfTeam) => scoreOfTeam.sortingIndex === index).length > 1;
  }

  getDrawWinner(index: number): Team[] {
    const teams: Team[] = [];
    if (this.teamScores && this.fightsFinished) {
      const scores: ScoreOfTeam[] = this.teamScores.filter((scoreOfTeam) => scoreOfTeam.sortingIndex === index);
      for (const scoreOfTeam of scores) {
        teams.push(scoreOfTeam.team);
      }
    }
    return teams;
  }

  closeDialog() {
    this.dialogRef.close();
  }

  downloadPDF() {
    if (this.tournament && this.tournament.id) {
      this.rankingService.getTeamsScoreRankingByTournamentAsPdf(this.tournament.id).subscribe((pdf: Blob) => {
        const blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL = window.URL.createObjectURL(blob);
        let pwa = window.open(downloadURL);
        if (!pwa || pwa.closed || typeof pwa.closed == 'undefined') {
          alert(this.translateService.instant('disablePopUpBlocker'));
        }
      });
    }
  }

  undrawTeams(index: number) {
    const teams: Team[] = this.getDrawWinner(index);
    let i = 1;
    from(teams).pipe(
      concatMap(() => {
        const dialogRef = this.dialog.open(UndrawTeamsComponent, {
          width: '90vw',
          data: {tournament: this.tournament, team1: teams[i - 1], team2: teams[i]}
        });
        i++;
        return dialogRef.afterClosed();
      }),
      takeWhile(Boolean)
    ).subscribe();
    this.closeDialog();
  }
}
