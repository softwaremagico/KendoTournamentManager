import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserNameComponent} from "./user-name.component";
import {MatIconModule} from "@angular/material/icon";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TranslocoModule} from "@ngneat/transloco";


@NgModule({
  declarations: [UserNameComponent],
  exports: [
    UserNameComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    DragDropModule,
    TranslocoModule
  ]
})
export class UserNameModule {
}
