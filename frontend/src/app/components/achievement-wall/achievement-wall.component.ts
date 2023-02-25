import {Component, Input, OnInit} from '@angular/core';
import {AchievementType} from "../../models/achievement-type.model";
import {Achievement} from "../../models/achievement.model";

@Component({
  selector: 'app-achievement-wall',
  templateUrl: './achievement-wall.component.html',
  styleUrls: ['./achievement-wall.component.scss']
})
export class AchievementWallComponent implements OnInit {

  totalAchievementsTypes: AchievementType[];

  @Input()
  achievements: Achievement[];

  constructor() {
    this.totalAchievementsTypes = AchievementType.toArray()
  }

  ngOnInit(): void {

  }

  getAchievements(achievementType: AchievementType): Achievement[] {
    return this.achievements.filter((a) => a.achievementType === achievementType);
  }
}
