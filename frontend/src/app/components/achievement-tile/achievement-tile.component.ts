import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {Achievement} from "../../models/achievement.model";
import {AchievementGrade} from "../../models/achievement-grade.model";
import {TranslateService} from "@ngx-translate/core";
import {formatDate} from "@angular/common";
import {AchievementType} from "../../models/achievement-type.model";

@Component({
  selector: 'app-achievement-tile',
  templateUrl: './achievement-tile.component.html',
  styleUrls: ['./achievement-tile.component.scss'],
  // tooltip style not applied without this:
  encapsulation: ViewEncapsulation.None,
})
export class AchievementTileComponent implements OnInit {

  @Input()
  achievements: Achievement[] | undefined;
  grade: AchievementGrade;


  constructor(private translateService: TranslateService) {

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

  tooltipText(): string {
    if (!this.achievements || this.achievements.length == 0) {
      return "";
    }
    // const parameters: object = {
    //   achievementType: this.translateService.instant(AchievementType.toCamel(this.achievements[0].achievementType)),
    //   achievementDescription: this.translateService.instant(AchievementType.toCamel(this.achievements[0].achievementType)+'Description')
    // };
    // return this.translateService.instant('achievementToolTip', parameters);
    let tooltipText: string = '<b>' + this.translateService.instant(AchievementType.toCamel(this.achievements[0].achievementType)) + '</b><br>' +
      this.translateService.instant(AchievementType.toCamel(this.achievements[0].achievementType) + 'Description') + '<br>';
    if (this.achievements) {
      tooltipText += '<br>Obtained at:<br>';
      tooltipText += '<div class="tournament-list">';
      for (const achievement of this.achievements) {
        if (achievement.tournament) {
          tooltipText += '<div class="tournament-item">';
          tooltipText += '<div class="circle ';
          if (achievement.achievementGrade == AchievementGrade.NORMAL) {
            tooltipText += ' normal"></div>';
          }
          if (achievement.achievementGrade == AchievementGrade.BRONZE) {
            tooltipText += ' bronze"></div>';
          }
          if (achievement.achievementGrade == AchievementGrade.SILVER) {
            tooltipText += ' silver"></div>';
          }
          if (achievement.achievementGrade == AchievementGrade.GOLD) {
            tooltipText += ' gold"></div>';
          }
          tooltipText += achievement.tournament.name;
          const formattedDate: string = formatDate(achievement.tournament.createdAt, 'dd/MM/yyyy', navigator.language)
          tooltipText += ' (' + formattedDate + ')';

          //End of tournament item.
          tooltipText += '</div>';
        }
      }
      tooltipText += '</div>';
    }
    return tooltipText;
  }

}
