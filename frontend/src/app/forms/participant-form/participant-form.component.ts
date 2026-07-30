import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {InputLimits} from "../../utils/input-limits";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Participant} from "../../models/participant";
import {ParticipantFormValidationFields} from "./participant-form-validation-fields";
import {RbacService} from "../../services/rbac/rbac.service";
import {TranslocoService} from '@jsverse/transloco';
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {ParticipantService} from "../../services/participant.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {Club} from "../../models/club";
import {ClubService} from "../../services/club.service";
import {ParticipantImage} from "../../models/participant-image.model";
import {PictureUpdatedService} from "../../services/notifications/picture-updated.service";
import {FileService} from "../../services/file.service";
import {MessageService} from "../../services/message.service";
import {takeUntil} from "rxjs";

@Component({
  standalone: false,
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
  saved: EventEmitter<Participant> = new EventEmitter<Participant>();
  @Input() @Output()
  errorEvent: EventEmitter<unknown> = new EventEmitter<unknown>();

  participantPicture: string | undefined = undefined;

  protected errors: Map<ParticipantFormValidationFields, string> = new Map<ParticipantFormValidationFields, string>();
  protected readonly ParticipantFormValidationFields = ParticipantFormValidationFields;
  protected translatedClubs: { value: string, label: string, description: string }[] = [];

  protected clubs: Club[] = [];
  protected saving: boolean = false;
  protected addPhoto: boolean = false;

  constructor(rbacService: RbacService, private readonly transloco: TranslocoService, private readonly biitSnackbarService: BiitSnackbarService,
              private readonly participantService: ParticipantService, private readonly clubService: ClubService,
               private readonly pictureUpdatedService: PictureUpdatedService, private readonly fileService: FileService,
               public readonly messageService: MessageService) {
    super(rbacService);
    this.loadClubs();
  }

  ngOnInit(): void {
    this.participantPicture = undefined;
    this.pictureUpdatedService.isPictureUpdated.pipe(takeUntil(this.destroySubject)).subscribe((_picture: string): void => {
      this.participantPicture = _picture;
    });
    if (this.participant?.id) {
      this.fileService.getParticipantPicture(this.participant).pipe(takeUntil(this.destroySubject)).subscribe((_picture: ParticipantImage): void => {
        if (_picture) {
          this.participantPicture = _picture.base64;
        } else {
          this.participantPicture = undefined;
        }
      });
    }
  }

  private loadClubs(): void {
    this.clubService.getAll().pipe(takeUntil(this.destroySubject)).subscribe((_clubs: Club[]) => {
      this.clubs = _clubs;
      this.translatedClubs = [];
      this.translateClubs(_clubs);
    });
  }

  private translateClubs(_clubs: Club[]): void {
    for (let club of _clubs) {
      this.translatedClubs.push({
        value: club.id + '', label: club.name, description: club.country + " (" + club.city + ")"
      });
    }
  }

  protected validate(): boolean {
    this.errors = new Map<ParticipantFormValidationFields, string>();
    let verdict: boolean = true;
    verdict = this.validateRequiredAndLength(this.participant.name, this.PARTICIPANT_NAME_MIN_LENGTH, this.PARTICIPANT_NAME_MAX_LENGTH, ParticipantFormValidationFields.NAME_ERRORS, verdict);
    verdict = this.validateRequiredAndLength(this.participant.lastname, this.PARTICIPANT_LASTNAME_MIN_LENGTH, this.PARTICIPANT_LASTNAME_MAX_LENGTH, ParticipantFormValidationFields.LASTNAME_ERRORS, verdict);
    if (this.participant.idCard && this.participant.idCard.length > this.PARTICIPANT_ID_MAX_LENGTH) {
      this.errors.set(ParticipantFormValidationFields.ID_CARD_ERRORS, this.transloco.translate(`v.maxLengthError`));
      verdict = false;
    }
    return verdict;
  }

  private validateRequiredAndLength(value: string | undefined, minLength: number, maxLength: number, field: ParticipantFormValidationFields, verdict: boolean): boolean {
    if (!value || value.length === 0) {
      this.errors.set(field, this.transloco.translate(`v.dataIsMandatory`));
      return false;
    }
    if (value.length < minLength) {
      this.errors.set(field, this.transloco.translate(`v.minLengthError`));
      return false;
    }
    if (value.length > maxLength) {
      this.errors.set(field, this.transloco.translate(`v.maxLengthError`));
      return false;
    }
    return verdict;
  }

  onSave(): void {
    if (!this.validate()) {
      this.biitSnackbarService.showNotification(this.transloco.translate('v.validationFailed'), NotificationType.WARNING);
      return;
    }

    this.saving = true;

    if (this.participant.id) {
      this.participantService.update(this.participant).subscribe({
        next: (participant: Participant): void => {
          this.saved.emit(participant);
        },
        error: error => ErrorHandler.notify(error, this.transloco as never, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    } else {
      this.participantService.add(this.participant).subscribe({
        next: (participant: Participant): void => {
          this.saved.emit(participant);
        },
        error: error => ErrorHandler.notify(error, this.transloco as never, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    }
  }

  setClub(clubId: string): void {
    this.participant.club = this.clubs.find(c => String(c.id) === clubId)!;
  }

  deletePicture(): void {
    this.fileService.deleteParticipantPicture(this.participant).pipe(takeUntil(this.destroySubject)).subscribe((): void => {
      this.messageService.infoMessage("pictureDeleted");
      this.participantPicture = undefined;
    });
  }
}
