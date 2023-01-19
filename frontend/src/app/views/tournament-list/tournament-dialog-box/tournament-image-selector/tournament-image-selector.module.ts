import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentImageSelectorComponent} from "./tournament-image-selector.component";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../../pipes/rbac-pipe/rbac.module";
import {TranslateModule} from "@ngx-translate/core";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";


@NgModule({
  declarations: [TournamentImageSelectorComponent],
  exports: [
    TournamentImageSelectorComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    RbacModule,
    TranslateModule,
    MatButtonModule,
    MatDialogModule
  ]
})
export class TournamentImageSelectorModule {
}
