import {Component, Inject, Optional} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Participant} from "../../models/participant";

export enum Action {
  Add,
  Update,
  Delete,
  Cancel
}

@Component({
  selector: 'app-participant-dialog-box',
  templateUrl: './participant-dialog-box.component.html',
  styleUrls: ['./participant-dialog-box.component.scss']
})
export class ParticipantDialogBoxComponent {

  participant: Participant;
  title: string;
  action: Action;
  actionName: string;

  constructor(
    public dialogRef: MatDialogRef<ParticipantDialogBoxComponent>,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Participant }) {
    this.participant = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
  }

  doAction() {
    this.dialogRef.close({data: this.participant, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

}
