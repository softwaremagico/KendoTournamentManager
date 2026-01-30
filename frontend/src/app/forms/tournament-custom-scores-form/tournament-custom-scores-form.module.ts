import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TournamentCustomScoresFormComponent } from './tournament-custom-scores-form.component';
import {TranslocoModule} from "@ngneat/transloco";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitInputTextModule} from "@biit-solutions/wizardry-theme/inputs";
import {MapGetPipeModule} from "@biit-solutions/wizardry-theme/utils";
import {FormsModule} from "@angular/forms";



@NgModule({
  declarations: [
    TournamentCustomScoresFormComponent
  ],
  exports: [
    TournamentCustomScoresFormComponent
  ],
  imports: [
    CommonModule,
    TranslocoModule,
    BiitButtonModule,
    BiitInputTextModule,
    MapGetPipeModule,
    FormsModule
  ]
})
export class TournamentCustomScoresFormModule { }
