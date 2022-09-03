import {Component, Inject, Optional} from '@angular/core';
import {Tournament} from "../../../models/tournament";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {TournamentType} from "../../../models/tournament-type";
import {Action} from "../../../action";
import {ScoreType} from "../../../models/score-type";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";

@Component({
  selector: 'app-tournament-dialog-box',
  templateUrl: './tournament-dialog-box.component.html',
  styleUrls: ['./tournament-dialog-box.component.scss']
})
export class TournamentDialogBoxComponent extends RbacBasedComponent {

  tournament: Tournament;
  title: string;
  action: Action;
  actionName: string;
  tournamentType: TournamentType[];
  scoreTypes: ScoreType[];

  constructor(
    public dialogRef: MatDialogRef<TournamentDialogBoxComponent>, rbacService: RbacService,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Tournament }) {
    super(rbacService)
    this.tournament = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.tournamentType = TournamentType.toArray();
    this.scoreTypes = ScoreType.toArray();
  }

  doAction() {
    this.dialogRef.close({data: this.tournament, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

}
