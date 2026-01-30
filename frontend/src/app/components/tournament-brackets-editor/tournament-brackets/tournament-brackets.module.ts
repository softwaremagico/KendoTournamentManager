import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentBracketsComponent} from "./tournament-brackets.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {GroupContainerComponent} from './group-container/group-container.component';
import {TeamCardModule} from "../../team-card/team-card.module";
import {ShiaijoModule} from "./shiaijo/shiaijo.module";
import {TranslocoModule} from "@ngneat/transloco";
import {KeyReversePipe} from "../../../pipes/keyReverse.pipe";
import {ArrowsModule} from "./arrows/arrows.module";


@NgModule({
  declarations: [TournamentBracketsComponent, GroupContainerComponent],
  exports: [TournamentBracketsComponent],
  imports: [
    CommonModule,
    DragDropModule,
    TeamCardModule,
    ShiaijoModule,
    TranslocoModule,
    KeyReversePipe,
    ArrowsModule
  ]
})
export class TournamentBracketsModule {
}
