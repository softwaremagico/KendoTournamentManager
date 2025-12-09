import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DropdownInterfacePipe} from "./dropdown-interface.pipe";



@NgModule({
  declarations: [DropdownInterfacePipe],
  exports: [
    DropdownInterfacePipe
  ],
  imports: [
    CommonModule,
  ]
})
export class DropdownInterfacePipeModule { }
