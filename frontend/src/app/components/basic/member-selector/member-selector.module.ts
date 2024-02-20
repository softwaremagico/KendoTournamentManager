import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MemberSelectorComponent} from "./member-selector.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {UserCardModule} from "../../user-card/user-card.module";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
  declarations: [MemberSelectorComponent],
  exports: [
    MemberSelectorComponent
  ],
  imports: [
    CommonModule,
    DragDropModule,
    UserCardModule,
    TranslateModule
  ]
})
export class MemberSelectorModule { }
