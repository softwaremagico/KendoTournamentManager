import {Component, Inject, Optional} from '@angular/core';
import {Tournament} from "../../../models/tournament";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {TournamentType} from "../../../models/tournament-type";
import {Action} from "../../../action";
import {ScoreType} from "../../../models/score-type";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {RbacActivity} from "../../../services/rbac/rbac.activity";
import {TournamentImageSelectorComponent} from "./tournament-image-selector/tournament-image-selector.component";
import {TournamentScoreEditorComponent} from "./tournament-score-editor/tournament-score-editor.component";
import {TranslateService} from "@ngx-translate/core";
import {TournamentExtraPropertiesComponent} from "./tournament-extra-properties/tournament-extra-properties.component";

@Component({
  selector: 'app-tournament-dialog-box',
  templateUrl: './tournament-dialog-box.component.html',
  styleUrls: ['./tournament-dialog-box.component.scss']
})
export class TournamentDialogBoxComponent extends RbacBasedComponent {

  tournament: Tournament;
  title: string;
  action: Action;
  actionName: string;
  tournamentType: TournamentType[];
  scoreTypes: ScoreType[];
  selectedType: TournamentType | undefined;
  typeLoop: TournamentType = TournamentType.LOOP;
  typeLeague: TournamentType = TournamentType.LEAGUE;
  typeKing: TournamentType = TournamentType.KING_OF_THE_MOUNTAIN;
  scoreTypeCustom: ScoreType = ScoreType.CUSTOM;
  selectedScore: ScoreType;

  registerForm: UntypedFormGroup;

  constructor(
    public dialogRef: MatDialogRef<TournamentDialogBoxComponent>, rbacService: RbacService,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Tournament },
    public dialog: MatDialog, private translateService: TranslateService) {
    super(rbacService)
    this.tournament = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.tournamentType = TournamentType.toArray();
    this.scoreTypes = ScoreType.toArray();
    this.selectedType = this.tournament.type;
    if (this.tournament.tournamentScore?.scoreType) {
      this.selectedScore = this.tournament.tournamentScore.scoreType;
    } else {
      this.selectedScore = ScoreType.INTERNATIONAL
    }

    this.registerForm = new UntypedFormGroup({
      tournamentName: new UntypedFormControl({
        value: this.tournament.name,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(4), Validators.maxLength(50)]),
      shiaijos: new UntypedFormControl({
        value: this.tournament.shiaijos,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.pattern("^[0-9]*$")]),
      tournamentType: new UntypedFormControl({
        value: this.tournament.type,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(2), Validators.maxLength(40)]),
      teamSize: new UntypedFormControl({
        value: this.tournament.teamSize,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.pattern("^[0-9]*$")]),
      duelsDuration: new UntypedFormControl({
        value: this.tournament.duelsDuration,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.maxLength(20)]),
      scoreTypes: new UntypedFormControl({
        value: this.tournament.tournamentScore?.scoreType,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required])
    },);

    this.disableShiaijos();
  }

  doAction() {
    this.tournament.name = this.registerForm.get('tournamentName')!.value;
    this.tournament.shiaijos = this.registerForm.get('shiaijos')!.value;
    this.tournament.type = this.registerForm.get('tournamentType')!.value;
    this.tournament.teamSize = this.registerForm.get('teamSize')!.value;
    this.tournament.duelsDuration = this.registerForm.get('duelsDuration')!.value;
    if (this.tournament.tournamentScore) {
      this.tournament.tournamentScore.scoreType = this.registerForm.get('scoreTypes')!.value;
    }
    this.closeDialog();
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel, data: this.tournament});
  }

  getTournamentTypeTranslationTag(tournamentType: TournamentType): string {
    if (!tournamentType) {
      return "";
    }
    return TournamentType.toCamel(tournamentType);
  }

  addPicture() {
    const dialogRef = this.dialog.open(TournamentImageSelectorComponent, {
      data: {
        title: "", action: Action.Add, tournament: this.tournament
      }
    });
    dialogRef.afterClosed().subscribe();
  }

  disableShiaijos() {
    if (this.selectedType == TournamentType.KING_OF_THE_MOUNTAIN || this.selectedType == TournamentType.LEAGUE || this.selectedType == TournamentType.LOOP) {
      this.registerForm.controls['shiaijos'].disable();
      this.registerForm.controls['shiaijos'].setValue(1);
    } else {
      this.registerForm.controls['shiaijos'].enable();
    }
  }

  select(type: TournamentType) {
    this.selectedType = type;
    this.disableShiaijos();
  }

  openScoreDefinition() {
    const dialogRef = this.dialog.open(TournamentScoreEditorComponent, {
      data: {
        title: this.translateService.instant('scoreRules'), action: Action.Add, tournament: this.tournament
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.tournament = result.data;
    });
  }

  selectScore(score: ScoreType): void {
    this.selectedScore = score;
  }

  openCustomProperties() {
    const dialogRef = this.dialog.open(TournamentExtraPropertiesComponent, {
      data: {
        title: this.translateService.instant('scoreRules'), action: Action.Add, tournament: this.tournament
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      this.tournament = result.data;
    });
  }
}
