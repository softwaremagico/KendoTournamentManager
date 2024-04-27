import {Component, Inject, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Club} from "../../../models/club";
import {Action} from "../../../action";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {RbacActivity} from "../../../services/rbac/rbac.activity";

@Component({
  selector: 'app-club-dialog-box',
  templateUrl: './club-dialog-box.component.html',
  styleUrls: ['./club-dialog-box.component.scss']
})
export class ClubDialogBoxComponent extends RbacBasedComponent {

  club: Club;
  title: string;
  action: Action;
  actionName: string;

  registerForm: UntypedFormGroup;

  constructor(
    public dialogRef: MatDialogRef<ClubDialogBoxComponent>, rbacService: RbacService,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Club }) {
    super(rbacService);
    this.club = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];

    this.registerForm = new UntypedFormGroup({
      clubName: new UntypedFormControl({
        value: this.club.name,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(4), Validators.maxLength(40)]),
      clubCountry: new UntypedFormControl({
        value: this.club.country,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(4), Validators.maxLength(40)]),
      clubCity: new UntypedFormControl({
        value: this.club.city,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(4), Validators.maxLength(20)]),
      clubAddress: new UntypedFormControl({
        value: this.club.address,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.minLength(4), Validators.maxLength(40)]),
      clubEmail: new UntypedFormControl({
        value: this.club.email,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.email]),
      clubPhone: new UntypedFormControl({
        value: this.club.phone,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.pattern("[- +()0-9]+"), Validators.minLength(4), Validators.maxLength(20)]),
      clubWeb: new UntypedFormControl({
        value: this.club.web,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.pattern('(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?'), Validators.minLength(4), Validators.maxLength(75)]),
    });
  }

  doAction() {
    this.club.name = this.registerForm.get('clubName')!.value;
    this.club.country = this.registerForm.get('clubCountry')!.value;
    this.club.city = this.registerForm.get('clubCity')!.value;
    this.club.address = this.registerForm.get('clubAddress')!.value;
    this.club.email = this.registerForm.get('clubEmail')!.value;
    this.club.phone = this.registerForm.get('clubPhone')!.value;
    this.club.web = this.registerForm.get('clubWeb')!.value;
    this.dialogRef.close({data: this.club, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

}
