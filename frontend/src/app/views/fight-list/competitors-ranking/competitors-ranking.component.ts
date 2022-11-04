import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Tournament} from "../../../models/tournament";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {RankingService} from "../../../services/ranking.service";
import {ScoreOfCompetitor} from "../../../models/score-of-competitor";
import {TranslateService} from "@ngx-translate/core";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";

@Component({
  selector: 'app-competitors-ranking',
  templateUrl: './competitors-ranking.component.html',
  styleUrls: ['./competitors-ranking.component.scss']
})
export class CompetitorsRankingComponent extends RbacBasedComponent implements OnInit {

  competitorsScore: ScoreOfCompetitor[];
  tournament: Tournament;

  constructor(public dialogRef: MatDialogRef<CompetitorsRankingComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament },
              private rankingService: RankingService, public translateService: TranslateService, rbacService: RbacService) {
    super(rbacService);
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
        const blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL = window.URL.createObjectURL(blob);
        let pwa = window.open(downloadURL);
        if (!pwa || pwa.closed || typeof pwa.closed == 'undefined') {
          alert(this.translateService.instant('disablePopUpBlocker'));
        }
      });
    }
  }
}
