import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Action} from "../../../action";
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from "@angular/forms";
import {UserRoles} from "../../../services/rbac/user-roles";

@Component({
  selector: 'app-authenticated-user-dialog-box',
  templateUrl: './authenticated-user-dialog-box.component.html',
  styleUrls: ['./authenticated-user-dialog-box.component.scss']
})
export class AuthenticatedUserDialogBoxComponent implements OnInit {

  authenticatedUser: AuthenticatedUser;
  title: string;
  action: Action;
  actionName: string;
  hidePassword: boolean = true;

  registerForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<AuthenticatedUserDialogBoxComponent>,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: AuthenticatedUser }) {
    this.authenticatedUser = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];

    this.registerForm = new FormGroup({
      username: new FormControl({
        value: this.authenticatedUser.username,
        disabled: this.action !== Action.Add
      }, Validators.required),
      name: new FormControl(this.authenticatedUser.name, [Validators.required, Validators.maxLength(20)]),
      lastname: new FormControl(this.authenticatedUser.lastname, [Validators.required, Validators.maxLength(40)]),
      roles: new FormControl({
        //TODO(softwaremagico): default values not set.
        value: this.authenticatedUser.roles,
        disabled: this.action === Action.Update && this.authenticatedUser.username === localStorage.getItem('username')!
      }, Validators.required),
      password: new FormControl('', [Validators.required, Validators.maxLength(40),
        Validators.pattern('^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{7,}$')]),
      repeatPassword: new FormControl('', Validators.required)
    }, {validators: confirmPasswordValidator});
  }

  ngOnInit(): void {
    this.registerForm.markAllAsTouched();
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

  get UserRoles(): typeof UserRoles {
    return UserRoles;
  }
}

export const confirmPasswordValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password');
  const repeatPassword = control.get('repeatPassword');
  return password && repeatPassword && password.value === repeatPassword.value ? null : {repeatPassword: false};
};
