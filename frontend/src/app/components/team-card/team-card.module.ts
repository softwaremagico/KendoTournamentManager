import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TeamCardComponent} from "./team-card.component";
import {MatCardModule} from "@angular/material/card";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TranslocoModule} from "@ngneat/transloco";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {ParticipantNamePipe} from "../../pipes/visualization/participant-name-pipe";
import {BiitIconModule} from "@biit-solutions/wizardry-theme/icon";


@NgModule({
  declarations: [TeamCardComponent],
  exports: [
    TeamCardComponent
  ],
  imports: [
    CommonModule,
    MatCardModule,
    DragDropModule,
    TranslocoModule,
    MatIconModule,
    MatTooltipModule,
    TranslocoModule,
    ParticipantNamePipe,
    BiitIconModule
  ]
})
export class TeamCardModule {
}
