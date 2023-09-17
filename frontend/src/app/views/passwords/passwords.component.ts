import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  UntypedFormControl,
  UntypedFormGroup,
  FormGroupDirective,
  NgForm,
  ValidationErrors,
  ValidatorFn,
  Validators
} from "@angular/forms";
import {ErrorStateMatcher} from "@angular/material/core";
import {UserService} from "../../services/user.service";
import {MessageService} from "../../services/message.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";

@Component({
  selector: 'app-passwords',
  templateUrl: './passwords.component.html',
  styleUrls: ['./passwords.component.scss']
})
export class PasswordsComponent extends RbacBasedComponent implements OnInit {

  hidePassword: boolean = true;
  oldPassword: string;
  newPassword: string;
  repeatedPassword: string;
  matcher = new MyErrorStateMatcher();

  passwordForm = new UntypedFormGroup({
    oldPassword: new UntypedFormControl('', Validators.required),
    newPassword: new UntypedFormControl('', [Validators.required, Validators.pattern('^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{7,}$')]),
    repeatPassword: new UntypedFormControl('', Validators.required)
  }, {validators: confirmPasswordValidator, updateOn: 'change'});

  constructor(private userService: UserService, private messageService: MessageService, rbacService: RbacService) {
    super(rbacService);
  }

  ngOnInit(): void {
    this.passwordForm.markAllAsTouched();
  }

  public changePassword() {
    this.userService.updatePassword(this.oldPassword, this.newPassword).subscribe(() => {
      this.messageService.infoMessage('infoAuthenticatedUserUpdated');
    });
  }

  public findInvalidControls() {
    const invalid = [];
    const controls = this.passwordForm.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    return invalid;
  }

  passwordMatch(): boolean {
    return !this.hidePassword && this.newPassword === this.repeatedPassword;
  }
}

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: UntypedFormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const invalidCtrl = !!(control?.invalid && control?.parent?.dirty);
    const invalidParent = !!(control?.parent?.invalid && control?.parent?.dirty);

    return invalidCtrl || invalidParent;
  }
}

export const confirmPasswordValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('newPassword');
  const repeatPassword = control.get('repeatPassword');
  return password && repeatPassword && password.value === repeatPassword.value ? null : {repeatPassword: true};
};
