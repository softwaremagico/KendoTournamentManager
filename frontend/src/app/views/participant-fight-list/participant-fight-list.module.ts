import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ParticipantFightListComponent} from './participant-fight-list.component';
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {TranslocoModule} from "@ngneat/transloco";
import {FightModule} from "../../components/fight/fight.module";
import {UntieFightModule} from "../../components/untie-fight/untie-fight.module";
import {FilterModule} from "../../components/basic/filter/filter.module";
import {UserScoreModule} from "../../components/fight/duel/user-score/user-score.module";
import {DuelModule} from "../../components/fight/duel/duel.module";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";


@NgModule({
  declarations: [
    ParticipantFightListComponent
  ],
  exports: [ParticipantFightListComponent],
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatSpinnerOverlayModule,
        MatTooltipModule,
        RbacModule,
        TranslocoModule,
        FightModule,
        UntieFightModule,
        FilterModule,
        UserScoreModule,
        DuelModule,
        HasPermissionPipe
    ]
})
export class ParticipantFightListModule {
}
