import {Component, Inject, OnInit, Optional} from '@angular/core';
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../../models/tournament";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {TranslateService} from "@ngx-translate/core";
import {MessageService} from "../../../../services/message.service";

@Component({
  selector: 'app-tournament-score-editor',
  templateUrl: './tournament-score-editor.component.html',
  styleUrls: ['./tournament-score-editor.component.scss']
})
export class TournamentScoreEditorComponent extends RbacBasedComponent implements OnInit {
  tournament: Tournament;

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament },
              public dialogRef: MatDialogRef<TournamentScoreEditorComponent>, rbacService: RbacService, public translateService: TranslateService,
              public messageService: MessageService,) {
    super(rbacService);
    this.tournament = data.tournament;
  }

  ngOnInit(): void {
  }

}
