import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {InputLimits} from "../../utils/input-limits";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Participant} from "../../models/participant";
import {ParticipantFormValidationFields} from "./participant-form-validation-fields";
import {RbacService} from "../../services/rbac/rbac.service";
import {TranslocoService} from "@ngneat/transloco";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {ParticipantService} from "../../services/participant.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {Club} from "../../models/club";
import {ClubService} from "../../services/club.service";

@Component({
  selector: 'participant-form',
  templateUrl: './participant-form.component.html',
  styleUrls: ['./participant-form.component.scss']
})
export class ParticipantFormComponent extends RbacBasedComponent implements OnInit {

  protected PARTICIPANT_NAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected PARTICIPANT_NAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected PARTICIPANT_LASTNAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected PARTICIPANT_LASTNAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected PARTICIPANT_ID_MAX_LENGTH: number = InputLimits.MAX_SMALL_FIELD_LENGTH;

  @Input()
  participant: Participant;
  @Input() @Output()
  onSaved: EventEmitter<Participant> = new EventEmitter<Participant>();
  @Input() @Output()
  onError: EventEmitter<any> = new EventEmitter<any>();

  protected errors: Map<ParticipantFormValidationFields, string> = new Map<ParticipantFormValidationFields, string>();
  protected readonly ParticipantFormValidationFields = ParticipantFormValidationFields;
  protected translatedClubs: { value: string, label: string, description: string }[] = [];

  clubs: Club[];
  protected saving: boolean = false;

  constructor(rbacService: RbacService, private transloco: TranslocoService, private biitSnackbarService: BiitSnackbarService,
              private participantService: ParticipantService, private clubService: ClubService) {
    super(rbacService);
  }

  ngOnInit() {
    this.translateClubs();
  }

  private translateClubs() {
    this.clubService.getAll().subscribe((_clubs: Club[]) => {
      this.clubs = _clubs;
      for (let club of _clubs) {
        this.translatedClubs.push({
          value: club.id + "", label: club.name, description: club.country + " (" + club.city + ")"
        });
      }
    });
  }

  protected validate(): boolean {
    this.errors = new Map<ParticipantFormValidationFields, string>();
    let verdict: boolean = true;
    if (!this.participant.name || this.participant.name.length == 0) {
      verdict = false;
      this.errors.set(ParticipantFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.dataIsMandatory`));
    } else {
      if (this.participant.name && this.participant.name.length < this.PARTICIPANT_NAME_MIN_LENGTH) {
        verdict = false;
        this.errors.set(ParticipantFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.minLengthError`));
      }
      if (this.participant.name && this.participant.name.length > this.PARTICIPANT_NAME_MAX_LENGTH) {
        verdict = false;
        this.errors.set(ParticipantFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.maxLengthError`));
      }
    }
    if (!this.participant.lastname || this.participant.lastname.length == 0) {
      verdict = false;
      this.errors.set(ParticipantFormValidationFields.LASTNAME_ERRORS, this.transloco.translate(`v.dataIsMandatory`));
    } else {
      if (this.participant.lastname && this.participant.lastname.length < this.PARTICIPANT_LASTNAME_MIN_LENGTH) {
        verdict = false;
        this.errors.set(ParticipantFormValidationFields.LASTNAME_ERRORS, this.transloco.translate(`v.minLengthError`));
      }
      if (this.participant.lastname && this.participant.lastname.length > this.PARTICIPANT_LASTNAME_MAX_LENGTH) {
        verdict = false;
        this.errors.set(ParticipantFormValidationFields.LASTNAME_ERRORS, this.transloco.translate(`v.maxLengthError`));
      }
    }
    if (this.participant.idCard && this.participant.idCard.length > this.PARTICIPANT_ID_MAX_LENGTH) {
      verdict = false;
      this.errors.set(ParticipantFormValidationFields.ID_CARD_ERRORS, this.transloco.translate(`v.maxLengthError`));
    }
    return verdict;
  }

  onSave() {
    if (!this.validate()) {
      this.biitSnackbarService.showNotification(this.transloco.translate('v.validationFailed'), NotificationType.WARNING);
      return;
    }

    this.saving = true;

    if (this.participant.id) {
      this.participantService.update(this.participant).subscribe({
        next: (participant: Participant): void => {
          this.onSaved.emit(participant);
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    } else {
      this.participantService.add(this.participant).subscribe({
        next: (participant: Participant): void => {
          this.onSaved.emit(participant);
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    }
  }

  setClub(clubId: string) {
    this.participant.club = this.clubs.filter(c => c.id + "" == clubId)![0];
  }
}
