import {Component, Inject, Optional} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Club} from "../../models/club";

export enum Action {
  Add,
  Update,
  Delete,
  Cancel
}

@Component({
  selector: 'app-club-dialog-box',
  templateUrl: './club-dialog-box.component.html',
  styleUrls: ['./club-dialog-box.component.css']
})
export class ClubDialogBoxComponent {

  club: Club;
  action: Action;
  ActionEnum: typeof Action = Action;

  constructor(
    public dialogRef: MatDialogRef<ClubDialogBoxComponent>,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: Club, action: Action) {
    this.club = data;
    this.action = action;
  }

  doAction() {
    this.dialogRef.close({data: this.club});
  }

  closeDialog() {
    this.dialogRef.close(Action.Cancel);
  }

}
