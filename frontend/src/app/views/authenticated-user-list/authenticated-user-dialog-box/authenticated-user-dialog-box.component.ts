import {Component, Inject, Optional} from '@angular/core';
import {Action} from "../../../action";
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {UserRoles} from "../../../services/rbac/user-roles";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacActivity} from "../../../services/rbac/rbac.activity";

@Component({
  selector: 'app-authenticated-user-dialog-box',
  templateUrl: './authenticated-user-dialog-box.component.html',
  styleUrls: ['./authenticated-user-dialog-box.component.scss']
})
export class AuthenticatedUserDialogBoxComponent extends RbacBasedComponent {

  authenticatedUser: AuthenticatedUser;
  title: string;
  action: Action;
  actionName: string;
  hidePassword: boolean = true;

  registerForm: UntypedFormGroup;

  constructor(
    public dialogRef: MatDialogRef<AuthenticatedUserDialogBoxComponent>, rbacService: RbacService,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: AuthenticatedUser }) {
    super(rbacService);
    this.authenticatedUser = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];

    this.registerForm = new UntypedFormGroup({
      username: new UntypedFormControl({
        value: this.authenticatedUser.username,
        disabled: this.action !== Action.Add
      }, Validators.required),
      name: new UntypedFormControl(this.authenticatedUser.name, [Validators.required, Validators.maxLength(20)]),
      lastname: new UntypedFormControl(this.authenticatedUser.lastname, [Validators.required, Validators.maxLength(40)]),
      roles: new UntypedFormControl({
        value: this.authenticatedUser.roles,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_USER) &&
          this.action === Action.Update && this.authenticatedUser.username === localStorage.getItem('username')!
      }, Validators.required),
      password: new UntypedFormControl('', [Validators.required, Validators.maxLength(40),
        Validators.pattern('^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{7,}$')]),
      repeatPassword: new UntypedFormControl('', Validators.required)
    },);
  }

  doAction(): void {
    this.authenticatedUser.username = this.registerForm.get('username')!.value;
    this.authenticatedUser.name = this.registerForm.get('name')!.value;
    this.authenticatedUser.lastname = this.registerForm.get('lastname')!.value;
    this.authenticatedUser.roles = this.registerForm.get('roles')!.value;
    this.authenticatedUser.password = this.registerForm.get('password')!.value;
    this.dialogRef.close({data: this.authenticatedUser, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({data: undefined, action: Action.Cancel});
  }

  passwordMatch(): boolean {
    return this.action != Action.Add || !this.hidePassword || this.registerForm.get('password')!.value === this.registerForm.get('repeatPassword')!.value;
  }

  public isOnCreation(): boolean {
    return this.action === Action.Add;
  }

  public setHidePassword(hidePassword: boolean) {
    this.hidePassword = hidePassword;
  }

  get UserRoles(): typeof UserRoles {
    return UserRoles;
  }
}
