import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {NumberDropdownInterfacePipe} from "./number-dropdown-interface.pipe";



@NgModule({
  declarations: [NumberDropdownInterfacePipe],
  exports: [
    NumberDropdownInterfacePipe
  ],
  imports: [
    CommonModule,
  ]
})
export class NumberDropdownInterfacePipeModule { }
