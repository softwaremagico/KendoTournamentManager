import {Component, Input, OnInit} from '@angular/core';
import {Achievement} from "../../models/achievement.model";

@Component({
  selector: 'app-achievement-tile',
  templateUrl: './achievement-tile.component.html',
  styleUrls: ['./achievement-tile.component.scss']
})
export class AchievementTileComponent implements OnInit {

  @Input()
  achievement: Achievement;

  constructor() {
  }

  ngOnInit(): void {
  }

  getAchievementImage(): String {
    return "assets/achievements/" + this.achievement.achievementType.toLowerCase() + ".svg";
  }

  getAchievementAlt(): String {
    return this.achievement.achievementType.toLowerCase();
  }

}
