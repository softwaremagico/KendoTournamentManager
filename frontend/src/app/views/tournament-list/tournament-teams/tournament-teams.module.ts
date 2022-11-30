import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TournamentTeamsComponent} from "./tournament-teams.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {UserListModule} from "../../../components/basic/user-list/user-list.module";
import {MatCardModule} from "@angular/material/card";
import {TranslateModule} from "@ngx-translate/core";
import {MatIconModule} from "@angular/material/icon";
import {FormsModule} from "@angular/forms";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";



@NgModule({
  declarations: [TournamentTeamsComponent],
  imports: [
    CommonModule,
    DragDropModule,
    UserListModule,
    MatCardModule,
    TranslateModule,
    MatIconModule,
    FormsModule,
    RbacModule,
    MatSpinnerOverlayModule
  ]
})
export class TournamentTeamsModule { }
