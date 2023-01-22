import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TournamentScoreEditorComponent} from "./tournament-score-editor.component";
import {TournamentImageSelectorComponent} from "../tournament-image-selector/tournament-image-selector.component";



@NgModule({
  declarations: [TournamentScoreEditorComponent],
  exports: [
    TournamentScoreEditorComponent
  ],
  imports: [
    CommonModule
  ]
})
export class TournamentScoreEditorModule { }
