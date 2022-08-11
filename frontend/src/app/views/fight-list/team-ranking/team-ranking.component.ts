import {Component, Inject, OnInit, Optional} from '@angular/core';
import {ScoreOfTeam} from "../../../models/score-of-team";
import {RankingService} from "../../../services/ranking.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../models/tournament";

@Component({
  selector: 'app-team-ranking',
  templateUrl: './team-ranking.component.html',
  styleUrls: ['./team-ranking.component.scss']
})
export class TeamRankingComponent implements OnInit {

  teamScores: ScoreOfTeam[];
  tournament: Tournament;

  constructor(public dialogRef: MatDialogRef<TeamRankingComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament },
              private rankingService: RankingService) {
    this.tournament = data.tournament;
  }

  ngOnInit(): void {
    if (this.tournament && this.tournament.id) {
      this.rankingService.getTeamsScoreRankingByTournament(this.tournament.id).subscribe(scoresOfTeams => {
        this.teamScores = scoresOfTeams;
      });
    }
  }

  closeDialog() {
    this.dialogRef.close();
  }

}
