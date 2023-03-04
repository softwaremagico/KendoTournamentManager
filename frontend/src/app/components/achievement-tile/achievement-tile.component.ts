import {Component, Input, OnInit} from '@angular/core';
import {Achievement} from "../../models/achievement.model";
import {AchievementGrade} from "../../models/achievement-grade.model";

@Component({
  selector: 'app-achievement-tile',
  templateUrl: './achievement-tile.component.html',
  styleUrls: ['./achievement-tile.component.scss']
})
export class AchievementTileComponent implements OnInit {

  @Input()
  achievements: Achievement[] | undefined;
  grade: AchievementGrade;


  constructor() {

  }

  ngOnInit(): void {
    this.grade = AchievementGrade.NORMAL;
    if (this.achievements) {
      for (const achievement of this.achievements) {
        if (achievement.achievementGrade == AchievementGrade.BRONZE &&
          this.grade != AchievementGrade.SILVER) {
          this.grade = AchievementGrade.BRONZE;
        }
        if (achievement.achievementGrade == AchievementGrade.SILVER) {
          this.grade = AchievementGrade.SILVER;
        }
        if (achievement.achievementGrade == AchievementGrade.GOLD) {
          this.grade = AchievementGrade.GOLD;
          break;
        }
      }
    }
  }

  getAchievementImage(): string {
    if (this.achievements && this.achievements.length > 0) {
      return "assets/achievements/" + this.achievements[0].achievementType.toLowerCase() + ".svg";
    }
    return "assets/achievements/default.svg";
  }

  getAchievementAlt(): string {
    if (this.achievements && this.achievements.length > 0) {
      return this.achievements[0].achievementType.toLowerCase();
    }
    return "";
  }

  isNewAchievement(): boolean {
    const today: Date = new Date();
    today.setDate(today.getDate() - 2);
    return this.achievements?.find(a => a.createdAt > today) !== undefined;
  }

  totalAchievements(): number {
    if (!this.achievements) {
      return 0;
    }
    return this.achievements?.length;
  }

  public get AchievementGrade() {
    return AchievementGrade;
  }

}
