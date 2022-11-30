import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {LeagueGeneratorComponent} from "./league-generator.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TeamListModule} from "../../../components/basic/team-list/team-list.module";
import {TranslateModule} from "@ngx-translate/core";
import {TeamCardModule} from "../../../components/team-card/team-card.module";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";



@NgModule({
  declarations: [LeagueGeneratorComponent],
  imports: [
    CommonModule,
    DragDropModule,
    TeamListModule,
    TranslateModule,
    TeamCardModule,
    MatIconModule,
    RbacModule,
    MatSpinnerOverlayModule,
    MatDialogModule,
    MatButtonModule
  ]
})
export class LeagueGeneratorModule { }
