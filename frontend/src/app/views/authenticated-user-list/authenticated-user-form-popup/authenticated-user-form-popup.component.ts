import {Component, EventEmitter, Input, Output} from '@angular/core';
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {
  AuthenticatedUserFormValidationFields
} from "../../../forms/authenticated-user-form/authenticated-user-form-validation-fields";

@Component({
  selector: 'authenticated-user-form-popup',
  templateUrl: './authenticated-user-form-popup.component.html',
  styleUrls: ['./authenticated-user-form-popup.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '/', alias: 't'}
    }
  ]
})
export class AuthenticatedUserFormPopupComponent {
  @Input() user: AuthenticatedUser;
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();
  @Output() onSaved: EventEmitter<AuthenticatedUser> = new EventEmitter<AuthenticatedUser>();
  @Output() onError: EventEmitter<any> = new EventEmitter<any>();

  protected errors: Map<AuthenticatedUserFormValidationFields, string> = new Map<AuthenticatedUserFormValidationFields, string>();

  constructor(protected transloco: TranslocoService) {
  }

}
