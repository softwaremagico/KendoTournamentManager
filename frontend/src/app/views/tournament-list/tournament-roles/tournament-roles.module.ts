import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TournamentRolesComponent} from "./tournament-roles.component";
import {AppModule} from "../../../app.module";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {UserListModule} from "../../../components/basic/user-list/user-list.module";
import {TranslateModule} from "@ngx-translate/core";
import {UserCardModule} from "../../../components/user-card/user-card.module";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";



@NgModule({
  declarations: [TournamentRolesComponent],
  imports: [
    CommonModule,
    DragDropModule,
    UserListModule,
    TranslateModule,
    UserCardModule,
    MatIconModule,
    RbacModule,
    MatSpinnerOverlayModule
  ]
})
export class TournamentRolesModule { }
