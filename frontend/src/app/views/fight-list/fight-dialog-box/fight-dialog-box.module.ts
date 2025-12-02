import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FightDialogBoxComponent} from "./fight-dialog-box.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslocoModule} from "@ngneat/transloco";
import {TeamCardModule} from "../../../components/team-card/team-card.module";
import {TeamListModule} from "../../../components/basic/team-list/team-list.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [FightDialogBoxComponent],
  imports: [
    CommonModule,
    DragDropModule,
    MatSpinnerOverlayModule,
    TranslocoModule,
    TeamCardModule,
    TeamListModule,
    MatDialogModule,
    MatButtonModule
  ]
})
export class FightDialogBoxModule { }
