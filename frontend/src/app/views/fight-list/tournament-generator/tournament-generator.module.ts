import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TournamentGeneratorComponent } from './tournament-generator.component';
import {
  TournamentBracketsEditorModule
} from "../../../components/tournament-brackets-editor/tournament-brackets-editor.module";



@NgModule({
  declarations: [
    TournamentGeneratorComponent
  ],
  exports:[
    TournamentGeneratorComponent
  ],
  imports: [
    CommonModule,
    TournamentBracketsEditorModule
  ]
})
export class TournamentGeneratorModule { }
