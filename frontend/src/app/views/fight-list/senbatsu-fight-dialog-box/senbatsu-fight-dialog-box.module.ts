import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SenbatsuFightDialogBoxComponent} from './senbatsu-fight-dialog-box.component';
import {DragDropModule} from "@angular/cdk/drag-drop";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {TeamCardModule} from "../../../components/team-card/team-card.module";
import {TeamListModule} from "../../../components/basic/team-list/team-list.module";
import {TranslocoModule} from "@ngneat/transloco";


@NgModule({
  declarations: [
    SenbatsuFightDialogBoxComponent
  ],
  imports: [
    CommonModule,
    DragDropModule,
    MatButtonModule,
    MatDialogModule,
    MatSpinnerOverlayModule,
    TeamCardModule,
    TeamListModule,
    TranslocoModule
  ]
})
export class SenbatsuFightDialogBoxModule {
}
