import {Component, Inject, Input, Optional, Output} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {RoleType} from "../../models/role-type";
import {Action} from "../../action";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-role-selector-dialog-box',
  templateUrl: './role-selector-dialog-box.component.html',
  styleUrls: ['./role-selector-dialog-box.component.scss']
})
export class RoleSelectorDialogBoxComponent {

  @Input()
  tournament: Tournament;

  @Output()
  roles: RoleType[] = [];

  roleTypes: RoleType[] = RoleType.toArray();

  constructor(public dialogRef: MatDialogRef<RoleSelectorDialogBoxComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    this.tournament = data.tournament;
  }

  setRoles(newOnes: boolean) {
    this.dialogRef.close({data: this.roles, newOnes: newOnes, action: Action.Selected});
  }

  closeDialog() {
    this.dialogRef.close({data: undefined, action: Action.Cancel});
  }

  select(checked: boolean, roleType: RoleType) {
    if (checked) {
      this.roles.push(roleType);
    } else {
      this.roles = this.roles.filter(item => item !== roleType);
    }
  }
}
