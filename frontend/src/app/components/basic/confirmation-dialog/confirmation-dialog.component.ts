import {Component} from '@angular/core';
import {MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent {

  public messageTag: string;

  constructor(public dialogRef: MatDialogRef<ConfirmationDialogComponent>) {
  }

  acceptAction() {
    this.dialogRef.close(true);
  }

  cancelDialog() {
    this.dialogRef.close(false);
  }
}
