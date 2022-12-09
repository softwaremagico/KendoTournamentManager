import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserCardComponent} from "./user-card.component";
import {MatCardModule} from "@angular/material/card";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {AppModule} from "../../app.module";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {TranslateModule} from "@ngx-translate/core";
import {MatIconModule} from "@angular/material/icon";


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
        RbacModule,
        TranslateModule,
        MatIconModule
    ]
})
export class UserCardModule {
}
