import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MemberSelectorComponent} from "./member-selector.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {UserCardModule} from "../../user-card/user-card.module";
import {TranslocoModule} from "@ngneat/transloco";


@NgModule({
  declarations: [MemberSelectorComponent],
  exports: [
    MemberSelectorComponent
  ],
  imports: [
    CommonModule,
    DragDropModule,
    UserCardModule,
    TranslocoModule
  ]
})
export class MemberSelectorModule { }
