import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {UserSessionService} from "../../../services/user-session.service";
import {TRANSLOCO_SCOPE, TranslocoService} from "@jsverse/transloco";
import {Participant} from "../../../models/participant";
import {ParticipantFormValidationFields} from "../../../forms/participant-form/participant-form-validation-fields";
import {CsvService} from "../../../services/csv-service";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {RbacActivity} from "../../../services/rbac/rbac.activity";

@Component({
  standalone: false,
  selector: 'participant-form-popup',
  templateUrl: './participant-form-popup.component.html',
  styleUrls: ['./participant-form-popup.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '/', alias: 't'}
    }
  ]
})
export class ParticipantFormPopupComponent implements OnInit {
  @Input() participant: Participant;
  @Output() closed: EventEmitter<void> = new EventEmitter<void>();
  @Output() saved: EventEmitter<Participant> = new EventEmitter<Participant>();
  @Output() errorEvent: EventEmitter<unknown> = new EventEmitter<unknown>();

  protected readonly RbacActivity = RbacActivity;

  protected errors: Map<ParticipantFormValidationFields, string> = new Map<ParticipantFormValidationFields, string>();
  protected loggedUser: AuthenticatedUser | undefined;

  constructor(protected sessionService: UserSessionService, private readonly csvService: CsvService,
              private readonly biitSnackbarService: BiitSnackbarService, protected transloco: TranslocoService) {
  }

  ngOnInit(): void {
    this.loggedUser = this.sessionService.getUser();
  }

  handleFileInput(event: Event): void {
    const element: HTMLInputElement = event.currentTarget as HTMLInputElement;
    const file: File | null | undefined = element.files?.item(0);
    if (!file) {
      return;
    }

    this.csvService.addParticipants(file).subscribe(_participants => {
      if (_participants.length === 0) {
        this.biitSnackbarService.showNotification(this.transloco.translate('infoParticipantStored'), NotificationType.SUCCESS);
        this.saved.emit();
      } else {
        const parameters: object = {element: _participants[0].name};
        this.biitSnackbarService.showNotification(this.transloco.translate('failedOnCsvField', parameters), NotificationType.ERROR);
      }
    });
  }
}
