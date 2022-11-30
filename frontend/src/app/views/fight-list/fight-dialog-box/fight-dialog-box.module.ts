import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FightDialogBoxComponent} from "./fight-dialog-box.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslateModule} from "@ngx-translate/core";
import {TeamCardModule} from "../../../components/team-card/team-card.module";
import {TeamListModule} from "../../../components/basic/team-list/team-list.module";



@NgModule({
  declarations: [FightDialogBoxComponent],
  imports: [
    CommonModule,
    DragDropModule,
    MatSpinnerOverlayModule,
    TranslateModule,
    TeamCardModule,
    TeamListModule
  ]
})
export class FightDialogBoxModule { }
