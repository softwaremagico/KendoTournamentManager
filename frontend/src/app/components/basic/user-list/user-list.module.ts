import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserListComponent} from "./user-list.component";
import {FilterModule} from "../filter/filter.module";
import {UserCardModule} from "../../user-card/user-card.module";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
  declarations: [UserListComponent],
  exports: [
    UserListComponent
  ],
  imports: [
    CommonModule,
    FilterModule,
    UserCardModule,
    TranslateModule
  ]
})
export class UserListModule {
}
