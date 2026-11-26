import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RbacPipe} from "./rbac.pipe";


@NgModule({
  declarations: [RbacPipe],
  exports: [
    RbacPipe
  ],
  imports: [
    CommonModule
  ]
})
export class RbacModule {
}
