import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {AchievementType} from "../../models/achievement-type.model";
import {Achievement} from "../../models/achievement.model";

@Component({
  selector: 'achievements-wall',
  templateUrl: './achievement-wall.component.html',
  styleUrls: ['./achievement-wall.component.scss']
})
export class AchievementWallComponent implements OnChanges {

  totalAchievementsTypes: AchievementType[];

  @Input()
  achievements: Achievement[];

  groupedAchievements: Map<AchievementType, Achievement[]>;

  @Input()
  view: 'participant' | 'tournament';

  constructor() {
    this.totalAchievementsTypes = AchievementType.toArray();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['achievements']) {
      if (this.achievements) {
        this.groupedAchievements = new Map<AchievementType, Achievement[]>();
        for (const achievementType of this.totalAchievementsTypes) {
          if (this.achievements) {
            this.groupedAchievements.set(achievementType, this.achievements.filter((a: Achievement): boolean => a.achievementType === achievementType));
          } else {
            this.groupedAchievements.set(achievementType, []);
          }
        }
      }
    }
  }
}
