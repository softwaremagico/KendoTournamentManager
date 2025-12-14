import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {UserSessionService} from "../../../services/user-session.service";
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {Participant} from "../../../models/participant";
import {ParticipantFormValidationFields} from "../../../forms/participant-form/participant-form-validation-fields";
import {CsvService} from "../../../services/csv-service";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {RbacActivity} from "../../../services/rbac/rbac.activity";

@Component({
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
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();
  @Output() onSaved: EventEmitter<Participant> = new EventEmitter<Participant>();
  @Output() onError: EventEmitter<any> = new EventEmitter<any>();

  protected readonly RbacActivity = RbacActivity;

  protected errors: Map<ParticipantFormValidationFields, string> = new Map<ParticipantFormValidationFields, string>();
  protected loggedUser: AuthenticatedUser | undefined;

  constructor(protected sessionService: UserSessionService, private csvService: CsvService,
              private biitSnackbarService: BiitSnackbarService, protected transloco: TranslocoService) {
  }

  ngOnInit(): void {
    this.loggedUser = this.sessionService.getUser();
  }

  handleFileInput(event: Event) {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList) {
      const file: File | null = fileList.item(0);
      if (file) {
        this.csvService.addParticipants(file).subscribe(_participants => {
          if (_participants.length == 0) {
            this.transloco.selectTranslate('infoParticipantStored').subscribe(
              translation => {
                this.biitSnackbarService.showNotification(translation, NotificationType.SUCCESS);
              }
            );
            this.onClosed.emit();
          } else {
            const parameters: object = {element: _participants[0].name};
            this.transloco.selectTranslate('failedOnCsvField', parameters).subscribe(
              translation => {
                this.biitSnackbarService.showNotification(translation, NotificationType.ERROR);
              }
            );
          }
        });
      }
    }
  }
}
