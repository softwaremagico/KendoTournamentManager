import {Component, ElementRef, Inject, OnInit, Optional} from '@angular/core';
import {Tournament} from "../../../models/tournament";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {RankingService} from "../../../services/ranking.service";
import {ScoreOfCompetitor} from "../../../models/score-of-competitor";

@Component({
  selector: 'app-competitors-ranking',
  templateUrl: './competitors-ranking.component.html',
  styleUrls: ['./competitors-ranking.component.scss']
})
export class CompetitorsRankingComponent implements OnInit {

  competitorsScore: ScoreOfCompetitor[];
  tournament: Tournament;

  constructor(public dialogRef: MatDialogRef<CompetitorsRankingComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament },
              private rankingService: RankingService) {
    this.tournament = data.tournament;
  }

  ngOnInit(): void {
    if (this.tournament && this.tournament.id) {
      this.rankingService.getCompetitorsScoreRankingByTournament(this.tournament.id).subscribe(competitorsScore => {
        this.competitorsScore = competitorsScore;
      });
    }
  }

  closeDialog() {
    this.dialogRef.close();
  }

  downloadPDF() {
    if (this.tournament && this.tournament.id) {
      this.rankingService.getCompetitorsScoreRankingByTournamentAsPdf(this.tournament.id).subscribe((pdf: Blob) => {
        // this.ref.nativeElement.href = window.URL.createObjectURL(pdf);
        // this.ref.nativeElement.click();
      });
    }
  }
}
