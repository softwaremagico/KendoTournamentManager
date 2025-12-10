import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {TournamentFormValidationFields} from "../../utils/validations/tournament-form-validation-fields";
import {ScoreType} from "../../models/score-type";
import {TournamentType} from "../../models/tournament-type";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {provideTranslocoScope, TranslocoService} from "@ngneat/transloco";
import {combineLatest} from "rxjs";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {InputLimits} from "../../utils/input-limits";
import {Type} from "@biit-solutions/wizardry-theme/inputs";
import {TournamentService} from "../../services/tournament.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";

@Component({
  selector: 'tournament-form',
  templateUrl: './tournament-form.component.html',
  styleUrls: ['./tournament-form.component.scss'],
  providers: [provideTranslocoScope({scope: '/', alias: ''}), provideTranslocoScope({scope: 'validation', alias: 'v'})]
})
export class TournamentFormComponent extends RbacBasedComponent implements OnInit {

  protected TOURNAMENT_NAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected TOURNAMENT_NAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected TOURNAMENT_MIN_SHIAIJO: number = 1;
  protected TOURNAMENT_MAX_SHIAIJO: number = 10;
  protected TOURNAMENT_MIN_TEAM: number = 1;
  protected TOURNAMENT_MAX_TEAM: number = 10;
  protected TOURNAMENT_ALLOWED_DURATION: number[] = [30, 60, 90, 120, 150, 180, 240, 300, 360, 420, 480, 540, 600];

  @Input()
  tournament: Tournament;
  @Input() @Output()
  onSaved: EventEmitter<Tournament> = new EventEmitter<Tournament>();
  @Input() @Output()
  onError: EventEmitter<any> = new EventEmitter<any>();

  protected errors: Map<TournamentFormValidationFields, string> = new Map<TournamentFormValidationFields, string>();
  protected readonly TournamentFormValidationFields = TournamentFormValidationFields;
  protected types = TournamentType.toArray();
  protected translatedTypes: { value: string, label: string, description: string }[] = [];
  protected scores = ScoreType.toArray();
  protected translatedScores: { value: string, label: string, description: string }[] = [];
  protected translatedDuration: { value: number, label: string }[] = [];
  selectedType: TournamentType | undefined;

  protected readonly TournamentType = TournamentType;

  typeLoop: TournamentType = TournamentType.LOOP;
  typeLeague: TournamentType = TournamentType.LEAGUE;
  typeKing: TournamentType = TournamentType.KING_OF_THE_MOUNTAIN;
  typeCustom: TournamentType = TournamentType.CUSTOMIZED;
  typeSorting: TournamentType = TournamentType.BUBBLE_SORT;
  scoreTypeCustom: ScoreType = ScoreType.CUSTOM;
  selectedScore: ScoreType;

  protected saving: boolean = false;


  constructor(rbacService: RbacService, private transloco: TranslocoService, private biitSnackbarService: BiitSnackbarService,
              private tournamentService: TournamentService,) {
    super(rbacService)
  }

  ngOnInit() {
    this.translateTypes();
    this.translateScores();
    this.translateDuration();
  }

  private translateTypes() {
    const typesTranslations = this.types.map(type => this.transloco.selectTranslate(`${TournamentType.toCamel(type)}`));
    combineLatest(typesTranslations).subscribe((translations) => {
      translations.forEach((label, index) => this.translatedTypes.push({
        value: this.types[index],
        label: label,
        description: this.transloco.translate(TournamentType.toCamel(this.types[index]) + "Hint")
      }));
    });
  }

  private translateScores() {
    const scoresTranslations = this.scores.map(score => this.transloco.selectTranslate(`${ScoreType.toCamel(score)}`));
    combineLatest(scoresTranslations).subscribe((translations) => {
      translations.forEach((label, index) => this.translatedScores.push({
        value: this.scores[index],
        label: label,
        description: this.transloco.translate(ScoreType.toCamel(this.scores[index]) + "Hint")
      }));
    });
  }

  private translateDuration() {
    for (let number of this.TOURNAMENT_ALLOWED_DURATION) {
      this.translatedDuration.push({
        value: number, label: this.getMinutes(number) + " " + this.transloco.translate('minutes') + " "
          + this.getSeconds(number) + " " + this.transloco.translate('seconds')
      });
    }
  }

  getMinutes(time: number): number {
    return ~~(time / 60);
  }

  getSeconds(time: number): number {
    return time % 60;
  }

  openCustomProperties() {

  }

  protected validate(): boolean {
    this.errors = new Map<TournamentFormValidationFields, string>();
    let verdict: boolean = true;
    if (!this.tournament.name || this.tournament.name.length == 0) {
      verdict = false;
      this.errors.set(TournamentFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.dataIsMandatory`));
    }
    if (this.tournament.name && this.tournament.name.length < this.TOURNAMENT_NAME_MIN_LENGTH) {
      verdict = false;
      this.errors.set(TournamentFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.minLengthError`));
    }
    if (this.tournament.name && this.tournament.name.length > this.TOURNAMENT_NAME_MAX_LENGTH) {
      verdict = false;
      this.errors.set(TournamentFormValidationFields.NAME_ERRORS, this.transloco.translate(`v.maxLengthError`));
    }
    if (this.tournament!.shiaijos! > this.TOURNAMENT_MAX_SHIAIJO) {
      verdict = false;
      this.errors.set(TournamentFormValidationFields.SHIAIJO_ERRORS, this.transloco.translate(`v.maxLengthError`));
    }
    if (this.tournament!.shiaijos! < this.TOURNAMENT_MIN_SHIAIJO) {
      verdict = false;
      this.errors.set(TournamentFormValidationFields.SHIAIJO_ERRORS, this.transloco.translate(`v.minLengthError`));
    }
    if (this.tournament!.teamSize! > this.TOURNAMENT_MAX_TEAM) {
      verdict = false;
      this.errors.set(TournamentFormValidationFields.TEAM_ERRORS, this.transloco.translate(`v.maxLengthError`));
    }
    if (this.tournament!.teamSize! < this.TOURNAMENT_MIN_TEAM) {
      verdict = false;
      this.errors.set(TournamentFormValidationFields.TEAM_ERRORS, this.transloco.translate(`v.minLengthError`));
    }
    return verdict;
  }

  onSave() {
    if (!this.validate()) {
      this.biitSnackbarService.showNotification(this.transloco.translate('v.validationFailed'), NotificationType.WARNING);
      return;
    }

    this.saving = true;

    if (this.tournament.id) {
      this.tournamentService.update(this.tournament).subscribe({
        next: (tournament: Tournament): void => {
          this.onSaved.emit(tournament);
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    }else{
      this.tournamentService.add(this.tournament).subscribe({
        next: (tournament: Tournament): void => {
          this.onSaved.emit(tournament);
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      }).add(() => {
        this.saving = false;
      });
    }
  }

  protected readonly Type = Type;
}
