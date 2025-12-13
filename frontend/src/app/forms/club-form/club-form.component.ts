import {Component, EventEmitter, Input, Output} from '@angular/core';
import {InputLimits} from "../../utils/input-limits";
import {Club} from "../../models/club";
import {ClubFormValidationFields} from "./club-form-validation-fields";
import {RbacService} from "../../services/rbac/rbac.service";
import {provideTranslocoScope, TranslocoService} from "@ngneat/transloco";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {TypeValidations} from "../../utils/type-validations";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {ClubService} from "../../services/club.service";

@Component({
  selector: 'club-form',
  templateUrl: './club-form.component.html',
  styleUrls: ['./club-form.component.scss'],
  providers: [provideTranslocoScope({scope: '/', alias: ''}), provideTranslocoScope({scope: 'validation', alias: 'v'})]
})
export class ClubFormComponent extends RbacBasedComponent {

  protected CLUB_NAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_NAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected CLUB_COUNTRY_MAX_LENGTH: number = InputLimits.MAX_SMALL_FIELD_LENGTH;
  protected CLUB_COUNTRY_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_CITY_MAX_LENGTH: number = InputLimits.MAX_SMALL_FIELD_LENGTH;
  protected CLUB_CITY_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_ADDRESS_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_ADDRESS_MAX_LENGTH: number = InputLimits.MAX_BIG_FIELD_LENGTH;
  protected CLUB_EMAIL_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected CLUB_PHONE_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_PHONE_MAX_LENGTH: number = InputLimits.MAX_SMALL_FIELD_LENGTH;
  protected CLUB_WEB_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_WEB_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;

  @Input()
  club: Club;

  @Input() @Output()
  onSaved: EventEmitter<Club> = new EventEmitter<Club>();
  @Input() @Output()
  onError: EventEmitter<any> = new EventEmitter<any>();

  protected errors: Map<ClubFormValidationFields, string> = new Map<ClubFormValidationFields, string>();
  protected readonly ClubFormValidationFields = ClubFormValidationFields;

  protected saving: boolean = false;

  constructor(rbacService: RbacService, private transloco: TranslocoService, private biitSnackbarService: BiitSnackbarService,
              private clubService: ClubService,) {
    super(rbacService)
  }

  protected validate(): boolean {
    this.errors = new Map<ClubFormValidationFields, string>();
    let verdict: boolean = true;
    if (!this.club.name || this.club.name.length == 0) {
      verdict = false;
      this.errors.set(ClubFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.dataIsMandatory`));
    } else {
      if (this.club.name && this.club.name.length < this.CLUB_NAME_MIN_LENGTH) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.minLengthError`));
      }
      if (this.club.name && this.club.name.length > this.CLUB_NAME_MAX_LENGTH) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.maxLengthError`));
      }
    }
    if (!this.club.country || this.club.country.length == 0) {
      verdict = false;
      this.errors.set(ClubFormValidationFields.COUNTRY_ERRORS, this.transloco.translate(`v.dataIsMandatory`));
    }else {
      if (this.club.country && this.club.country.length < this.CLUB_COUNTRY_MIN_LENGTH) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.COUNTRY_ERRORS, this.transloco.translate(`v.minLengthError`));
      }
      if (this.club.country && this.club.country.length > this.CLUB_COUNTRY_MAX_LENGTH) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.COUNTRY_ERRORS, this.transloco.translate(`v.maxLengthError`));
      }
    }
    if (!this.club.city || this.club.city.length == 0) {
      verdict = false;
      this.errors.set(ClubFormValidationFields.CITY_ERRORS, this.transloco.translate(`v.dataIsMandatory`));
    }else {
      if (this.club.city && this.club.city.length < this.CLUB_CITY_MIN_LENGTH) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.CITY_ERRORS, this.transloco.translate(`v.minLengthError`));
      }
      if (this.club.city && this.club.city.length > this.CLUB_CITY_MAX_LENGTH) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.CITY_ERRORS, this.transloco.translate(`v.maxLengthError`));
      }
    }
    if (this.club.email) {
      if (!TypeValidations.isEmail(this.club.email)) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.EMAIL_ERRORS, this.transloco.translate(`v.formatInvalid`));
      }
    }
    if (this.club.email && this.club.email.length > this.CLUB_EMAIL_MAX_LENGTH) {
      verdict = false;
      this.errors.set(ClubFormValidationFields.EMAIL_ERRORS, this.transloco.translate(`v.maxLengthError`));
    }
    if (this.club.phone) {
      if (!TypeValidations.isPhoneNumber(this.club.phone)) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.PHONE_ERRORS, this.transloco.translate(`v.formatInvalid`));
      }
    }
    if (this.club.phone && this.club.phone.length < this.CLUB_PHONE_MIN_LENGTH) {
      verdict = false;
      this.errors.set(ClubFormValidationFields.PHONE_ERRORS, this.transloco.translate(`v.minLengthError`));
    }
    if (this.club.phone && this.club.phone.length > this.CLUB_PHONE_MAX_LENGTH) {
      verdict = false;
      this.errors.set(ClubFormValidationFields.PHONE_ERRORS, this.transloco.translate(`v.maxLengthError`));
    }
    if (this.club.web) {
      if (!TypeValidations.isWebPage(this.club.web)) {
        verdict = false;
        this.errors.set(ClubFormValidationFields.WEB_ERRORS, this.transloco.translate(`v.formatInvalid`));
      }
    }
    if (this.club.web && this.club.web.length < this.CLUB_WEB_MIN_LENGTH) {
      verdict = false;
      this.errors.set(ClubFormValidationFields.WEB_ERRORS, this.transloco.translate(`v.minLengthError`));
    }
    if (this.club.web && this.club.web.length > this.CLUB_WEB_MAX_LENGTH) {
      verdict = false;
      this.errors.set(ClubFormValidationFields.WEB_ERRORS, this.transloco.translate(`v.maxLengthError`));
    }
    return verdict;
  }

  onSave() {
    if (!this.validate()) {
      this.biitSnackbarService.showNotification(this.transloco.translate('v.validationFailed'), NotificationType.WARNING);
      return;
    }

    this.saving = true;

    if (this.club.id) {
      this.clubService.update(this.club).subscribe({
        next: (club: Club): void => {
          this.onSaved.emit(club);
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    } else {
      this.clubService.add(this.club).subscribe({
        next: (club: Club): void => {
          this.onSaved.emit(club);
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    }
  }
}
