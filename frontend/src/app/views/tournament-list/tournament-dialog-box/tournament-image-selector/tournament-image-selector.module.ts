import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentImageSelectorComponent} from "./tournament-image-selector.component";


@NgModule({
  declarations: [TournamentImageSelectorComponent],
  exports: [
    TournamentImageSelectorComponent
  ],
  imports: [
    CommonModule
  ]
})
export class TournamentImageSelectorModule {
}
