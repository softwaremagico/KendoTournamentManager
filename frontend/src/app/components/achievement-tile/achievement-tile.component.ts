import {Component, Input, OnInit} from '@angular/core';
import {Achievement} from "../../models/achievement.model";

@Component({
  selector: 'app-achievement-tile',
  templateUrl: './achievement-tile.component.html',
  styleUrls: ['./achievement-tile.component.scss']
})
export class AchievementTileComponent implements OnInit {

  @Input()
  achievements: Achievement[] | undefined;


  constructor() {
  }

  ngOnInit(): void {
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

}
