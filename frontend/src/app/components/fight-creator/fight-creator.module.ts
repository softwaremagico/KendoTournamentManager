import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FightCreator} from "./fight-creator.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {MatSpinnerOverlayModule} from "../mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslocoModule} from "@ngneat/transloco";
import {TeamCardModule} from "../team-card/team-card.module";
import {TeamListModule} from "../basic/team-list/team-list.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";


@NgModule({
  declarations: [FightCreator],
  exports: [
    FightCreator
  ],
  imports: [
    CommonModule,
    DragDropModule,
    MatSpinnerOverlayModule,
    TranslocoModule,
    TeamCardModule,
    TeamListModule,
    MatDialogModule,
    MatButtonModule,
    BiitButtonModule
  ]
})
export class FightCreatorModule {
}
