import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {UserSessionService} from "../../../services/user-session.service";
import {TranslocoService} from "@ngneat/transloco";
import {Participant} from "../../../models/participant";
import {ParticipantFormValidationFields} from "../../../forms/participant-form/participant-form-validation-fields";

@Component({
  selector: 'participant-form-popup',
  templateUrl: './participant-form-popup.component.html',
  styleUrls: ['./participant-form-popup.component.scss']
})
export class ParticipantFormPopupComponent implements OnInit {
  @Input() participant: Participant;
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();
  @Output() onSaved: EventEmitter<Participant> = new EventEmitter<Participant>();
  @Output() onError: EventEmitter<any> = new EventEmitter<any>();

  protected errors: Map<ParticipantFormValidationFields, string> = new Map<ParticipantFormValidationFields, string>();
  protected loggedUser: AuthenticatedUser | undefined;

  constructor(protected sessionService: UserSessionService,
              protected transloco: TranslocoService) {
  }

  ngOnInit(): void {
    this.loggedUser = this.sessionService.getUser();
  }
}
