import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FightStatisticsPanelComponent} from "./fight-statistics-panel.component";
import {MatIconModule} from "@angular/material/icon";



@NgModule({
  declarations: [FightStatisticsPanelComponent],
  imports: [
    CommonModule,
    MatIconModule,
  ]
})
export class FightStatisticsPanelModule { }
