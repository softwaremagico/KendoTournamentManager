import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentBracketsEditorComponent} from './tournament-brackets-editor.component';
import {TournamentBracketsModule} from "./tournament-brackets/tournament-brackets.module";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TeamCardModule} from "../team-card/team-card.module";
import {TeamListModule} from "../basic/team-list/team-list.module";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {TranslocoModule} from "@ngneat/transloco";
import {MatDialogModule} from "@angular/material/dialog";
import {MatTooltipModule} from "@angular/material/tooltip";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {BiitProgressBarModule} from "@biit-solutions/wizardry-theme/info";


@NgModule({
  declarations: [
    TournamentBracketsEditorComponent
  ],
  exports:[
    TournamentBracketsEditorComponent
  ],
    imports: [
        CommonModule,
        TournamentBracketsModule,
        DragDropModule,
        TeamCardModule,
        TeamListModule,
        MatButtonModule,
        MatIconModule,
        RbacModule,
        TranslocoModule,
        MatDialogModule,
        MatTooltipModule,
        HasPermissionPipe,
        BiitButtonModule,
        BiitPopupModule,
        BiitIconButtonModule,
        BiitProgressBarModule
    ]
})
export class TournamentBracketsEditorModule { }
