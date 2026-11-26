import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {TranslocoService} from '@jsverse/transloco';
import {TournamentFormValidationFields} from "../tournament-form/tournament-form-validation-fields";
import {Type} from "@biit-solutions/wizardry-theme/inputs";

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'tournament-custom-scores-form',
  templateUrl: './tournament-custom-scores-form.component.html',
  styleUrls: ['./tournament-custom-scores-form.component.scss']
})
export class TournamentCustomScoresFormComponent extends RbacBasedComponent {

  @Input()
  tournament: Tournament;
  @Output()
  closed: EventEmitter<void> = new EventEmitter<void>();

  constructor(rbacService: RbacService, public translateService: TranslocoService) {
    super(rbacService);
  }

  protected readonly TournamentFormValidationFields = TournamentFormValidationFields;
  protected readonly Type = Type;
}
