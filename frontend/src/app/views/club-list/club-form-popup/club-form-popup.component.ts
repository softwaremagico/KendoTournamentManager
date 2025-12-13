import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {UserSessionService} from "../../../services/user-session.service";
import {Club} from "../../../models/club";
import {ClubFormValidationFields} from "../../../forms/club-form/club-form-validation-fields";

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

  constructor(protected sessionService: UserSessionService,
              protected transloco: TranslocoService) {
  }

  ngOnInit(): void {
    this.loggedUser = this.sessionService.getUser();
  }
}
