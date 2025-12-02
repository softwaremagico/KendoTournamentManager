import {Component, OnInit} from '@angular/core';
import {MatDialogRef} from "@angular/material/dialog";
import {TranslocoService} from "@ngneat/transloco";

@Component({
  selector: 'app-confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent implements OnInit {

  message: string;
  messageTag: string;
  parameters: object;

  constructor(public dialogRef: MatDialogRef<ConfirmationDialogComponent>, private translateService: TranslocoService) {

  }

  acceptAction(): void {
    this.dialogRef.close(true);
  }

  cancelDialog(): void {
    this.dialogRef.close(false);
  }

  ngOnInit(): void {
    this.message = "";
    this.message += this.translateService.translate(this.messageTag, this.parameters);
  }
}
