import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FaultComponent} from "./fault.component";
import {MatMenuModule} from "@angular/material/menu";
import {TranslateModule} from "@ngx-translate/core";



@NgModule({
  declarations: [FaultComponent],
  exports: [
    FaultComponent
  ],
  imports: [
    CommonModule,
    MatMenuModule,
    TranslateModule
  ]
})
export class FaultModule { }
