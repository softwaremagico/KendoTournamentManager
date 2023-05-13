import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AchievementWallComponent} from "./achievement-wall.component";
import {AchievementTileModule} from "../achievement-tile/achievement-tile.module";


@NgModule({
  declarations: [AchievementWallComponent],
  exports: [
    AchievementWallComponent
  ],
  imports: [
    CommonModule,
    AchievementTileModule
  ]
})
export class AchievementWallModule { }
