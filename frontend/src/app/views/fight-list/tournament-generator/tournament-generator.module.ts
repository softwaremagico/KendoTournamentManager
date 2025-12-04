import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentGeneratorComponent} from './tournament-generator.component';
import {
  TournamentBracketsEditorModule
} from "../../../components/tournament-brackets-editor/tournament-brackets-editor.module";
import {MatSpinnerOverlayModule} from "../../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {TranslocoModule} from "@ngneat/transloco";
import {MatDividerModule} from "@angular/material/divider";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";


@NgModule({
  declarations: [
    TournamentGeneratorComponent
  ],
  exports:[
    TournamentGeneratorComponent
  ],
    imports: [
        CommonModule,
        TournamentBracketsEditorModule,
        MatSpinnerOverlayModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        RbacModule,
        TranslocoModule,
        MatDividerModule,
        HasPermissionPipe
    ]
})
export class TournamentGeneratorModule { }
