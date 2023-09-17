import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentBracketsComponent} from "./tournament-brackets.component";
import {ArrowModule} from "./arrow/arrow.module";
import {DragDropModule} from "@angular/cdk/drag-drop";
import { GroupContainerComponent } from './group-container/group-container.component';
import {TeamCardModule} from "../../team-card/team-card.module";


@NgModule({
  declarations: [TournamentBracketsComponent, GroupContainerComponent],
  exports: [TournamentBracketsComponent],
  imports: [
    CommonModule,
    ArrowModule,
    DragDropModule,
    TeamCardModule
  ]
})
export class TournamentBracketsModule {
}
