import {Component, OnInit} from '@angular/core';
import {MatDialogRef} from "@angular/material/dialog";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent implements OnInit {

  message: string;
  messageTag: string;
  parameters: object;

  constructor(public dialogRef: MatDialogRef<ConfirmationDialogComponent>, private translateService: TranslateService) {

  }

  acceptAction(): void {
    this.dialogRef.close(true);
  }

  cancelDialog(): void {
    this.dialogRef.close(false);
  }

  ngOnInit(): void {
    this.message = "";
    this.translateService.get(this.messageTag, this.parameters).subscribe((res: string): void => {
      this.message += res;
    });
  }
}
