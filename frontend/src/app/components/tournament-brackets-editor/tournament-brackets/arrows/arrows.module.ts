import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ArrowsComponent} from './arrows.component';


@NgModule({
  declarations: [
    ArrowsComponent
  ],
  exports: [ArrowsComponent],
  imports: [
    CommonModule
  ]
})
export class ArrowsModule {
}
