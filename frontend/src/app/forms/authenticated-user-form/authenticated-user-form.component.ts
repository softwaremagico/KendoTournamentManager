import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthenticatedUser} from "../../models/authenticated-user";
import {AuthenticatedUserFormValidationFields} from "./authenticated-user-form-validation-fields";
import {RbacService} from "../../services/rbac/rbac.service";
import {provideTranslocoScope, TranslocoService} from "@ngneat/transloco";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {InputLimits} from "../../utils/input-limits";
import {UserSessionService} from "../../services/user-session.service";
import {PasswordGenerator} from "../../utils/random/password-generator";
import {Type} from "@biit-solutions/wizardry-theme/inputs";
import {UserService} from "../../services/user.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {Observable} from "rxjs";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {ActivityService} from "../../services/rbac/activity.service";

@Component({
  selector: 'authenticated-user-form',
  templateUrl: './authenticated-user-form.component.html',
  styleUrls: ['./authenticated-user-form.component.scss'],
  providers: [provideTranslocoScope({scope: '/', alias: 't'}), provideTranslocoScope({scope: 'validation', alias: 'v'})]
})
export class AuthenticatedUserFormComponent extends RbacBasedComponent implements OnInit {

  @Input()
  user: AuthenticatedUser;
  @Input() @Output()
  onSaved: EventEmitter<AuthenticatedUser> = new EventEmitter<AuthenticatedUser>();
  @Input() @Output()
  onError: EventEmitter<any> = new EventEmitter<any>();

  protected USERNAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected USERNAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected NAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected NAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected LASTNAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected LASTNAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected PASSWORD_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;

  protected errors: Map<AuthenticatedUserFormValidationFields, string> = new Map<AuthenticatedUserFormValidationFields, string>();

  protected readonly AuthenticatedUserFormValidationFields = AuthenticatedUserFormValidationFields;

  protected saving: boolean = false;
  protected loggedUser: AuthenticatedUser | undefined = undefined;

  protected pwdVerification: string = '';
  protected oldPassword: string;

  protected readonly Type = Type;


  constructor(rbacService: RbacService, private transloco: TranslocoService, private biitSnackbarService: BiitSnackbarService,
              private userService: UserService, private sessionService: UserSessionService, private activityService: ActivityService) {
    super(rbacService)
  }

  ngOnInit() {
    if (!this.user?.id) {
      this.generatePassword();
    }
    this.loggedUser = this.sessionService.getUser();
  }

  protected onSave(): void {
    if (!this.validate()) {
      this.biitSnackbarService.showNotification(this.transloco.translate('v.validationFailed'), NotificationType.WARNING);
      return;
    }
    if (this.user.id && this.user.password && this.user.password === this.pwdVerification) {
      const observable: Observable<void> = this.activityService.isAllowed(RbacActivity.CHANGE_OTHERS_PASSWORD)
        ? this.userService.updateUserPassword(this.user.username, this.user.password)
        : this.userService.updatePassword(this.oldPassword, this.user.password);
      observable.subscribe({
        next: (): void => {
          this.biitSnackbarService.showNotification(this.transloco.translate('infoPasswordUpdated'), NotificationType.SUCCESS);
        }, error: (): void => {
          this.biitSnackbarService.showNotification(this.transloco.translate('invalidPassword'), NotificationType.WARNING);
        }
      })
    }
    if (this.user.id) {
      this.userService.update(this.user).subscribe({
        next: (_authenticatedUser: AuthenticatedUser): void => {
          this.onSaved.emit(_authenticatedUser);
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    } else {
      this.userService.add(this.user).subscribe({
        next: (_authenticatedUser: AuthenticatedUser): void => {
          this.onSaved.emit(_authenticatedUser);
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    }
  }

  protected validate(): boolean {
    this.errors = new Map<AuthenticatedUserFormValidationFields, string>();
    let verdict: boolean = true;

    if (this.user.username && this.user.username.indexOf(" ") >= 0) {
      verdict = false;
      this.errors.set(AuthenticatedUserFormValidationFields.USERNAME_INVALID, this.transloco.translate(`v.usernameInvalid`));
    }
    if (!this.user.name) {
      verdict = false;
      this.errors.set(AuthenticatedUserFormValidationFields.NAME_MANDATORY, this.transloco.translate(`v.dataIsMandatory`));
    }
    if (!this.user.lastname) {
      verdict = false;
      this.errors.set(AuthenticatedUserFormValidationFields.LASTNAME_MANDATORY, this.transloco.translate(`v.dataIsMandatory`));
    }
    if (!this.user.id) {
      if (!this.user.password) {
        verdict = false;
        this.errors.set(AuthenticatedUserFormValidationFields.PASSWORD_MANDATORY, this.transloco.translate(`v.dataIsMandatory`));
      }
      if (this.pwdVerification !== this.user.password) {
        verdict = false;
        this.errors.set(AuthenticatedUserFormValidationFields.PASSWORD_MISMATCH, this.transloco.translate(`v.passwordMismatch`));
      }
    } else {
      if (this.user.password && this.pwdVerification !== this.user.password) {
        verdict = false;
        this.errors.set(AuthenticatedUserFormValidationFields.PASSWORD_MISMATCH, this.transloco.translate(`v.passwordMismatch`));
      }
    }

    return verdict;
  }

  protected generatePassword(): void {
    this.user.password = PasswordGenerator.generate();
    this.pwdVerification = this.user.password;
  }
}
