import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UntieFightComponent} from "./untie-fight.component";
import {MatIconModule} from "@angular/material/icon";
import {DuelModule} from "../fight/duel/duel.module";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
  declarations: [UntieFightComponent],
  exports: [
    UntieFightComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    DuelModule,
    TranslateModule
  ]
})
export class UntieFightModule { }
