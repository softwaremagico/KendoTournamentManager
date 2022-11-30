import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserCardComponent} from "./user-card.component";
import {MatCardModule} from "@angular/material/card";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {AppModule} from "../../app.module";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";


@NgModule({
  declarations: [
    UserCardComponent
  ],
  exports: [
    UserCardComponent
  ],
  imports: [
    CommonModule,
    MatCardModule,
    DragDropModule,
    AppModule,
    RbacModule
  ]
})
export class UserCardModule {
}
