import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {TranslocoService} from "@ngneat/transloco";
import {TournamentFormValidationFields} from "../tournament-form/tournament-form-validation-fields";
import {Type} from "@biit-solutions/wizardry-theme/inputs";

@Component({
  selector: 'tournament-custom-scores-form',
  templateUrl: './tournament-custom-scores-form.component.html',
  styleUrls: ['./tournament-custom-scores-form.component.scss']
})
export class TournamentCustomScoresFormComponent extends RbacBasedComponent implements OnInit {

  @Input()
  tournament: Tournament;
  @Output()
  onClosed: EventEmitter<void> = new EventEmitter<void>();

  constructor(rbacService: RbacService, public translateService: TranslocoService) {
    super(rbacService);
  }

  ngOnInit(): void {

  }

  protected readonly TournamentFormValidationFields = TournamentFormValidationFields;
  protected readonly Type = Type;
}
