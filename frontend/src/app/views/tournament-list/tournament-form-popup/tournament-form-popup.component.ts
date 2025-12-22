import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {Tournament} from "../../../models/tournament";
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {UserSessionService} from "../../../services/user-session.service";
import {TournamentFormValidationFields} from "../../../forms/tournament-form/tournament-form-validation-fields";
import {RbacActivity} from "../../../services/rbac/rbac.activity";

@Component({
  selector: 'tournament-form-popup',
  templateUrl: './tournament-form-popup.component.html',
  styleUrls: ['./tournament-form-popup.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '/', alias: 't'}
    }
  ]
})
export class TournamentFormPopupComponent implements OnInit {
  @Input() tournament: Tournament;
  originalTournament: Tournament;
  @Output() onClosed: EventEmitter<Tournament> = new EventEmitter<Tournament>();
  @Output() onSaved: EventEmitter<Tournament> = new EventEmitter<Tournament>();
  @Output() onError: EventEmitter<any> = new EventEmitter<any>();

  protected errors: Map<TournamentFormValidationFields, string> = new Map<TournamentFormValidationFields, string>();
  protected loggedUser: AuthenticatedUser | undefined;

  constructor(protected sessionService: UserSessionService,
              protected transloco: TranslocoService) {
  }

  ngOnInit(): void {
    this.loggedUser = this.sessionService.getUser();
    this.originalTournament = Tournament.clone(this.tournament);
  }

  protected readonly RbacActivity = RbacActivity;
}
