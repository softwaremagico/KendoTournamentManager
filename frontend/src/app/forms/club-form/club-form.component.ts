import {Component, EventEmitter, Input, Output} from '@angular/core';
import {InputLimits} from "../../utils/input-limits";
import {Club} from "../../models/club";
import {ClubFormValidationFields} from "./club-form-validation-fields";
import {RbacService} from "../../services/rbac/rbac.service";
import {provideTranslocoScope, TranslocoService} from "@jsverse/transloco";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {ClubService} from "../../services/club.service";
import {validateClubForm} from "./club-form-validation";

@Component({
  standalone: false,
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
  saved: EventEmitter<Club> = new EventEmitter<Club>();
  @Input() @Output()
  errorEvent: EventEmitter<any> = new EventEmitter<any>();

  protected errors: Map<ClubFormValidationFields, string> = new Map<ClubFormValidationFields, string>();
  protected readonly ClubFormValidationFields = ClubFormValidationFields;

  protected saving: boolean = false;

  constructor(rbacService: RbacService, private readonly transloco: TranslocoService, private readonly biitSnackbarService: BiitSnackbarService,
              private readonly clubService: ClubService,) {
    super(rbacService)
  }

  protected validate(): boolean { // NOSONAR
    this.errors = new Map<ClubFormValidationFields, string>();
    return validateClubForm(this.club, this.transloco, this.errors);
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
          this.saved.emit(club);
        },
        error: error => ErrorHandler.notify(error, this.transloco as never, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    } else {
      this.clubService.add(this.club).subscribe({
        next: (club: Club): void => {
          this.saved.emit(club);
        },
        error: error => ErrorHandler.notify(error, this.transloco as never, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    }
  }
}
