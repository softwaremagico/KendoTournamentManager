import {Component, Inject, Optional} from '@angular/core';
import {RbacBasedComponent} from "../../RbacBasedComponent";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {RbacService} from "../../../services/rbac/rbac.service";
import {Action} from "../../../action";

@Component({
  selector: 'app-picture-dialog-box',
  templateUrl: './picture-dialog-box.component.html',
  styleUrls: ['./picture-dialog-box.component.scss']
})
export class PictureDialogBoxComponent extends RbacBasedComponent {

  participantPicture: string;

  constructor(public dialogRef: MatDialogRef<PictureDialogBoxComponent>, rbacService: RbacService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { image: string }, public dialog: MatDialog,) {
    super(rbacService);
    this.participantPicture = data.image;
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

}
