import {NgModule} from '@angular/core';
import {TournamentListComponent} from "./tournament-list.component";
import {CommonModule} from "@angular/common";
import {TournamentRoutingModule} from "./tournament-routing.module";
import {MatIconModule} from "@angular/material/icon";
import {TranslateModule} from "@ngx-translate/core";
import {BasicTableModule} from "../../components/basic/basic-table/basic-table.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [TournamentListComponent],
  exports: [TournamentListComponent],
  imports: [
    TournamentRoutingModule,
    CommonModule,
    MatIconModule,
    TranslateModule,
    BasicTableModule,
    MatTooltipModule,
    MatButtonModule,
    RbacModule,
    MatSpinnerOverlayModule
  ]
})
export class TournamentListModule {
}
