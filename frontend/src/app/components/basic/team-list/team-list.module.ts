import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TeamListComponent} from "./team-list.component";
import {FilterModule} from "../filter/filter.module";
import {TeamCardModule} from "../../team-card/team-card.module";
import {TranslocoModule} from "@ngneat/transloco";


@NgModule({
  declarations: [TeamListComponent],
  exports: [
    TeamListComponent
  ],
  imports: [
    CommonModule,
    FilterModule,
    TeamCardModule,
    TranslocoModule
  ]
})
export class TeamListModule { }
