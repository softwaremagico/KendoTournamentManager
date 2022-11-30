import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TimerComponent} from "./timer.component";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {MatIconModule} from "@angular/material/icon";



@NgModule({
  declarations: [TimerComponent],
  exports: [
    TimerComponent
  ],
  imports: [
    CommonModule,
    RbacModule,
    MatIconModule
  ]
})
export class TimerModule { }
