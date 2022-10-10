import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Action} from "../../../action";
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from "@angular/forms";

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
  repeatedPassword: string;


  registerForm = new FormGroup({
    username: new FormControl('', Validators.required),
    name: new FormControl('', Validators.required),
    lastname: new FormControl('', Validators.required),
    password: new FormControl('',  [Validators.required, Validators.pattern('^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{7,}$')]),
    repeatPassword: new FormControl('', Validators.required)
  }, { validators: confirmPasswordValidator});

  constructor(
    public dialogRef: MatDialogRef<AuthenticatedUserDialogBoxComponent>,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: AuthenticatedUser }) {
    this.authenticatedUser = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.repeatedPassword = "";
  }

  ngOnInit(): void {
    this.registerForm.controls['username'].markAsTouched();
    this.registerForm.controls['name'].markAsTouched();
    this.registerForm.controls['lastname'].markAsTouched();
    this.registerForm.controls['password'].markAsTouched();
    this.registerForm.controls['repeatPassword'].markAsTouched();
  }

  doAction() {
    this.dialogRef.close({data: this.authenticatedUser, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

  passwordMatch(): boolean {
    return this.action != Action.Add || !this.hidePassword || this.authenticatedUser.password === this.repeatedPassword;
  }
}

export const confirmPasswordValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password');
  const repeatPassword = control.get('repeatPassword');
  return password && repeatPassword && password.value === repeatPassword.value ? { repeatPassword: true } : { repeatPassword: false };
};
