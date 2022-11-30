import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TeamListComponent} from "./team-list.component";
import {FilterModule} from "../filter/filter.module";
import {TeamCardModule} from "../../team-card/team-card.module";



@NgModule({
  declarations: [TeamListComponent],
  exports: [
    TeamListComponent
  ],
  imports: [
    CommonModule,
    FilterModule,
    TeamCardModule
  ]
})
export class TeamListModule { }
