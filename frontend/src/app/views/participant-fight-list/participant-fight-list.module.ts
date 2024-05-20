import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ParticipantFightListComponent} from './participant-fight-list.component';
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {TranslateModule} from "@ngx-translate/core";
import {FightModule} from "../../components/fight/fight.module";
import {UntieFightModule} from "../../components/untie-fight/untie-fight.module";
import {FilterModule} from "../../components/basic/filter/filter.module";
import {UserScoreModule} from "../../components/fight/duel/user-score/user-score.module";


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
        TranslateModule,
        FightModule,
        UntieFightModule,
        FilterModule,
        UserScoreModule
    ]
})
export class ParticipantFightListModule {
}
