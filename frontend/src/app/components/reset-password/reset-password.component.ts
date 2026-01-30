import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Type} from "@biit-solutions/wizardry-theme/inputs";
import {InputLimits} from "../../utils/input-limits";
import {PasswordFormValidationFields} from "./password-form-validation-fields";
import {AuthenticatedUser} from "../../models/authenticated-user";
import {UserService} from "../../services/user.service";
import {provideTranslocoScope, TranslocoService} from "@ngneat/transloco";
import {UserSessionService} from "../../services/user-session.service";
import {PasswordGenerator} from "../../utils/random/password-generator";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {MessageService} from "../../services/message.service";
import {BiitProgressBarType, BiitSnackbarService} from "@biit-solutions/wizardry-theme/info";

@Component({
  selector: 'reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss'],
  providers: [provideTranslocoScope({scope: '/', alias: ''}), provideTranslocoScope({scope: 'validation', alias: 'v'})]
})
export class ResetPasswordComponent implements OnInit {

  @Input() user: AuthenticatedUser;
  @Input() @Output() onSaved: EventEmitter<AuthenticatedUser> = new EventEmitter<AuthenticatedUser>();
  @Input() @Output() onError: EventEmitter<any> = new EventEmitter<any>();
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();

  protected PASSWORD_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;

  protected password: string[] = ["", ""];
  protected readonly Type = Type;
  protected errors: Map<PasswordFormValidationFields, string> = new Map<PasswordFormValidationFields, string>();
  protected complete = false;

  loadingGlobal: boolean = false;

  protected readonly PasswordFormValidationFields = PasswordFormValidationFields;
  protected readonly RbacActivity = RbacActivity;


  protected pwdVerification: string = '';
  protected newPassword: string;
  protected oldPassword: string;
  protected loggedUser: AuthenticatedUser | undefined;


  constructor(private userService: UserService,
              protected sessionService: UserSessionService,
              protected transloco: TranslocoService,
              private biitSnackbarService: BiitSnackbarService,
              private messageService: MessageService) {
  }

  ngOnInit(): void {
    this.loggedUser = this.sessionService.getUser();
    if (!this.user?.id) {
      this.generatePassword();
    }
  }

  protected generatePassword(): void {
    if (this.user) {
      this.newPassword = PasswordGenerator.generate();
      this.pwdVerification = this.newPassword;
    }
  }

  close() {
    this.onClosed.emit();
  }

  changePassword(): void {
    if (this.validate()) {
      this.loadingGlobal = true;
      this.userService.updatePassword(this.oldPassword, this.newPassword).subscribe(()=>{
        this.messageService.infoMessage('infoAuthenticatedUserUpdated');
        this.close();
      }).add(() => this.loadingGlobal = false);
    }
  }

  protected validate(): boolean {
    let verdict: boolean = true;
    if (!this.oldPassword) {
      verdict = false;
      this.errors.set(PasswordFormValidationFields.OLD_PASSWORD_MANDATORY, this.transloco.translate(`v.dataIsMandatory`));
    }
    if (!this.newPassword) {
      verdict = false;
      this.errors.set(PasswordFormValidationFields.PASSWORD_MANDATORY, this.transloco.translate(`v.dataIsMandatory`));
    }
    if (this.pwdVerification !== this.newPassword) {
      verdict = false;
      this.errors.set(PasswordFormValidationFields.PASSWORD_MISMATCH, this.transloco.translate(`v.passwordMismatch`));
    }
    return verdict;
  }

  protected readonly BiitProgressBarType = BiitProgressBarType;
}
