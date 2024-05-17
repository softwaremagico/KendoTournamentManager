import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ScoreComponent} from "./score.component";
import {MatMenuModule} from "@angular/material/menu";
import {TranslateModule} from "@ngx-translate/core";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../../../pipes/rbac-pipe/rbac.module";


@NgModule({
  declarations: [ScoreComponent],
  exports: [
    ScoreComponent
  ],
    imports: [
        CommonModule,
        MatMenuModule,
        TranslateModule,
        MatTooltipModule,
        MatIconModule,
        RbacModule
    ]
})
export class ScoreModule {
}
