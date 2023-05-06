import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FaultComponent} from "./fault.component";
import {MatMenuModule} from "@angular/material/menu";
import {TranslateModule} from "@ngx-translate/core";
import {MatTooltipModule} from "@angular/material/tooltip";



@NgModule({
  declarations: [FaultComponent],
  exports: [
    FaultComponent
  ],
  imports: [
    CommonModule,
    MatMenuModule,
    TranslateModule,
    MatTooltipModule
  ]
})
export class FaultModule { }
