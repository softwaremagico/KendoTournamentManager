import {NgModule} from '@angular/core';
import {FightListComponent} from "./fight-list.component";
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {TranslocoModule} from "@ngneat/transloco";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {FightModule} from "../../components/fight/fight.module";
import {UntieFightModule} from "../../components/untie-fight/untie-fight.module";
import {TimerModule} from "../../components/timer/timer.module";
import {FilterModule} from "../../components/basic/filter/filter.module";
import {MatDividerModule} from "@angular/material/divider";
import {CommonModule} from "@angular/common";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatButtonModule} from "@angular/material/button";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";


@NgModule({
  declarations: [FightListComponent],
    imports: [
        MatSpinnerOverlayModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
        RbacModule,
        FightModule,
        UntieFightModule,
        TimerModule,
        FilterModule,
        MatDividerModule,
        CommonModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        HasPermissionPipe
    ]
})
export class FightListModule {
}
