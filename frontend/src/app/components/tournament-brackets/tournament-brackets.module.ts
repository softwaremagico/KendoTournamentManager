import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentBracketsComponent} from "./tournament-brackets.component";
import {ArrowModule} from "./arrow/arrow.module";


@NgModule({
  declarations: [TournamentBracketsComponent],
  exports: [TournamentBracketsComponent],
  imports: [
    CommonModule,
    ArrowModule
  ]
})
export class TournamentBracketsModule {
}
