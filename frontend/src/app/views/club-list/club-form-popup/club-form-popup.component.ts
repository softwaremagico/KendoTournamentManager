import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {UserSessionService} from "../../../services/user-session.service";
import {Club} from "../../../models/club";
import {ClubFormValidationFields} from "../../../forms/club-form/club-form-validation-fields";
import {CsvService} from "../../../services/csv-service";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {RbacActivity} from "../../../services/rbac/rbac.activity";

@Component({
  selector: 'club-form-popup',
  templateUrl: './club-form-popup.component.html',
  styleUrls: ['./club-form-popup.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '/', alias: 't'}
    }
  ]
})
export class ClubFormPopupComponent implements OnInit {
  @Input() club: Club;
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();
  @Output() onSaved: EventEmitter<Club> = new EventEmitter<Club>();
  @Output() onError: EventEmitter<any> = new EventEmitter<any>();

  protected errors: Map<ClubFormValidationFields, string> = new Map<ClubFormValidationFields, string>();
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
        this.csvService.addClubs(file).subscribe(_clubs => {
          if (_clubs.length == 0) {
            this.transloco.selectTranslate('clubStored').subscribe(
              translation => {
                this.biitSnackbarService.showNotification(translation, NotificationType.SUCCESS);
              }
            );
            this.onClosed.emit();
          } else {
            const parameters: object = {element: _clubs[0].name};
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

    protected readonly RbacActivity = RbacActivity;
}
