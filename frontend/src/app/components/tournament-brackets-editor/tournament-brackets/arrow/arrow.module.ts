import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ArrowComponent} from "./arrow.component";


@NgModule({
  declarations: [ArrowComponent],
  exports: [ArrowComponent],
  imports: [
    CommonModule
  ]
})
export class ArrowModule {
}
