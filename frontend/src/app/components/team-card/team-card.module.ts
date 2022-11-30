import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TeamCardComponent} from "./team-card.component";
import {MatCardModule} from "@angular/material/card";
import {DragDropModule} from "@angular/cdk/drag-drop";



@NgModule({
  declarations: [TeamCardComponent],
  exports: [
    TeamCardComponent
  ],
  imports: [
    CommonModule,
    MatCardModule,
    DragDropModule
  ]
})
export class TeamCardModule { }
