import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AchievementTileComponent} from "./achievement-tile.component";


@NgModule({
  declarations: [AchievementTileComponent],
  exports: [
    AchievementTileComponent
  ],
  imports: [
    CommonModule
  ]
})
export class AchievementTileModule {
}
