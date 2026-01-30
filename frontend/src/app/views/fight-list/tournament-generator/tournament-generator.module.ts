import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentGeneratorComponent} from './tournament-generator.component';
import {
  TournamentBracketsEditorModule
} from "../../../components/tournament-brackets-editor/tournament-brackets-editor.module";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {TranslocoModule} from "@ngneat/transloco";
import {MatDividerModule} from "@angular/material/divider";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {BiitProgressBarModule} from "@biit-solutions/wizardry-theme/info";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [
    TournamentGeneratorComponent
  ],
  exports: [
    TournamentGeneratorComponent
  ],
  imports: [
    CommonModule,
    TournamentBracketsEditorModule,
    MatSpinnerOverlayModule,
    MatButtonModule,
    MatIconModule,
    RbacModule,
    TranslocoModule,
    MatDividerModule,
    HasPermissionPipe,
    BiitButtonModule,
    BiitPopupModule,
    BiitIconButtonModule,
    BiitProgressBarModule
  ]
})
export class TournamentGeneratorModule {
}
