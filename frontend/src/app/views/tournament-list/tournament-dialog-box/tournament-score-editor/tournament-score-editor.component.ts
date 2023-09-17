import {Component, Inject, OnInit, Optional} from '@angular/core';
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../../models/tournament";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {TranslateService} from "@ngx-translate/core";
import {MessageService} from "../../../../services/message.service";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {RbacActivity} from "../../../../services/rbac/rbac.activity";
import {Action} from "../../../../action";

@Component({
  selector: 'app-tournament-score-editor',
  templateUrl: './tournament-score-editor.component.html',
  styleUrls: ['./tournament-score-editor.component.scss']
})
export class TournamentScoreEditorComponent extends RbacBasedComponent {
  tournament: Tournament;
  title: string;

  formScore: UntypedFormGroup;

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, tournament: Tournament },
              public dialogRef: MatDialogRef<TournamentScoreEditorComponent>, rbacService: RbacService, public translateService: TranslateService,
              public messageService: MessageService,) {
    super(rbacService);
    this.tournament = data.tournament;
    this.title = data.title;

    this.formScore = new UntypedFormGroup({
      pointsByVictory: new UntypedFormControl({
        value: this.tournament.tournamentScore.pointsByVictory,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT_SCORE)
      }, [Validators.required, Validators.pattern("^[0-9]*$")]),
      pointsByDraw: new UntypedFormControl({
        value: this.tournament.tournamentScore.pointsByDraw,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT_SCORE)
      }, [Validators.required, Validators.pattern("^[0-9]*$")]),
    });
  }

  setTournamentPoints() {
    this.tournament.tournamentScore.pointsByVictory = this.formScore.get('pointsByVictory')!.value;
    this.tournament.tournamentScore.pointsByDraw = this.formScore.get('pointsByDraw')!.value;
    this.closeDialog();
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Update, data: this.tournament});
  }
}
