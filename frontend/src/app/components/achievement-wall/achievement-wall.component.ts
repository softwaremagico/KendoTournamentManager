import {Component, Input} from '@angular/core';
import {AchievementType} from "../../models/achievement-type.model";
import {Achievement} from "../../models/achievement.model";

@Component({
  selector: 'app-achievement-wall',
  templateUrl: './achievement-wall.component.html',
  styleUrls: ['./achievement-wall.component.scss']
})
export class AchievementWallComponent {

  totalAchievementsTypes: AchievementType[];

  @Input()
  achievements: Achievement[];

  constructor() {
    this.totalAchievementsTypes = AchievementType.toArray()
  }

  getAchievements(achievementType: AchievementType): Achievement[] {
    if (this.achievements) {
      return this.achievements.filter((a: Achievement): boolean => a.achievementType === achievementType);
    }
    return [];
  }
}
