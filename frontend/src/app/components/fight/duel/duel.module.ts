import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DuelComponent} from "./duel.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {UserScoreModule} from "./user-score/user-score.module";
import {DrawModule} from "./draw/draw.module";
import {TranslocoModule} from "@ngneat/transloco";


@NgModule({
  declarations: [DuelComponent],
  exports: [
    DuelComponent
  ],
  imports: [
    CommonModule,
    DragDropModule,
    UserScoreModule,
    DrawModule,
    TranslocoModule
  ]
})
export class DuelModule {
}
