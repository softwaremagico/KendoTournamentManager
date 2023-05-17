import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TeamListComponent} from "./team-list.component";
import {FilterModule} from "../filter/filter.module";
import {TeamCardModule} from "../../team-card/team-card.module";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
  declarations: [TeamListComponent],
  exports: [
    TeamListComponent
  ],
  imports: [
    CommonModule,
    FilterModule,
    TeamCardModule,
    TranslateModule
  ]
})
export class TeamListModule { }
