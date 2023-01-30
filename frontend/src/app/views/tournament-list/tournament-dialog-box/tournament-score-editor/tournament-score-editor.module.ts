import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentScoreEditorComponent} from "./tournament-score-editor.component";
import {ReactiveFormsModule} from "@angular/forms";
import {MatSpinnerOverlayModule} from "../../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDialogModule} from "@angular/material/dialog";
import {TranslateModule} from "@ngx-translate/core";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {RbacModule} from "../../../../pipes/rbac-pipe/rbac.module";
import {MatIconModule} from "@angular/material/icon";


@NgModule({
  declarations: [TournamentScoreEditorComponent],
  exports: [
    TournamentScoreEditorComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatSpinnerOverlayModule,
    MatFormFieldModule,
    MatDialogModule,
    TranslateModule,
    MatInputModule,
    MatButtonModule,
    RbacModule,
    MatIconModule
  ]
})
export class TournamentScoreEditorModule { }
