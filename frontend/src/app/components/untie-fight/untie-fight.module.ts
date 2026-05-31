import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UntieFightComponent} from "./untie-fight.component";
import {MatIconModule} from "@angular/material/icon";
import {DuelModule} from "../fight/duel/duel.module";
import {TranslocoModule} from "@ngneat/transloco";


@NgModule({
  declarations: [UntieFightComponent],
  exports: [
    UntieFightComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    DuelModule,
    TranslocoModule
  ]
})
export class UntieFightModule {
}
