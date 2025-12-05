import {NgModule} from '@angular/core';
import {TournamentListComponent} from "./tournament-list.component";
import {CommonModule, DatePipe} from "@angular/common";
import {TournamentRoutingModule} from "./tournament-routing.module";
import {MatIconModule} from "@angular/material/icon";
import {TranslocoModule} from "@ngneat/transloco";
import {BasicTableModule} from "../../components/basic/basic-table/basic-table.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatButtonModule} from "@angular/material/button";
import {BiitDatatableModule} from "@biit-solutions/wizardry-theme/table";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {MatDividerModule} from "@angular/material/divider";

@NgModule({
  declarations: [TournamentListComponent],
  exports: [TournamentListComponent],
    imports: [
        TournamentRoutingModule,
        CommonModule,
        MatIconModule,
        TranslocoModule,
        BasicTableModule,
        MatTooltipModule,
        MatButtonModule,
        RbacModule,
        MatSpinnerOverlayModule,
        BiitDatatableModule,
        HasPermissionPipe,
        BiitIconButtonModule,
        BiitButtonModule,
        MatDividerModule
    ],
  providers: [
    DatePipe
  ]
})
export class TournamentListModule {
}
