import {Component, Inject, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Club} from "../../../models/club";
import {Action} from "../../../action";

@Component({
  selector: 'app-club-dialog-box',
  templateUrl: './club-dialog-box.component.html',
  styleUrls: ['./club-dialog-box.component.scss']
})
export class ClubDialogBoxComponent {

  club: Club;
  title: string;
  action: Action;
  actionName: string;

  constructor(
    public dialogRef: MatDialogRef<ClubDialogBoxComponent>,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Club }) {
    this.club = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
  }

  doAction() {
    this.dialogRef.close({data: this.club, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

}
