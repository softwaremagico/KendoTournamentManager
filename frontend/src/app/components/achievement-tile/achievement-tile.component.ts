import {Component, HostListener, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {Achievement} from "../../models/achievement.model";
import {AchievementGrade} from "../../models/achievement-grade.model";
import {TranslateService} from "@ngx-translate/core";
import {formatDate} from "@angular/common";
import {AchievementType} from "../../models/achievement-type.model";
import {NameUtilsService} from "../../services/name-utils.service";

@Component({
  selector: 'app-achievement-tile',
  templateUrl: './achievement-tile.component.html',
  styleUrls: ['./achievement-tile.component.scss'],
  // tooltip style not applied without this:
  encapsulation: ViewEncapsulation.None,
})
export class AchievementTileComponent implements OnInit {

  @Input()
  achievementType: AchievementType;

  @Input()
  achievements: Achievement[] | undefined;

  @Input()
  view: 'participant' | 'tournament';

  grade: AchievementGrade;
  mouseX: number | undefined;
  mouseY: number | undefined;
  screenHeight: number | undefined;
  screenWidth: number | undefined;
  onLeftBorder: boolean;
  onRightBorder: boolean;

  constructor(private translateService: TranslateService, private nameUtils: NameUtilsService) {

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

  @HostListener('window:resize', ['$event'])
  onResize(event: Event): void {
    this.calculateTooltipMargin();
  }

  getAchievementImage(): string {
    return "assets/achievements/" + this.achievementType.toLowerCase() + ".svg";
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
    return this.achievements?.find((a: Achievement): boolean => a.createdAt > today) !== undefined;
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
    if (this.view = 'tournament') {
      return this.tournamentToolTipText();
    } else {
      return this.participantToolTipText();
    }
  }

  participantToolTipText(): string {
    if (!this.achievements || this.achievements.length == 0) {
      return "";
    }
    let tooltipText: string = '<b>' + this.translateService.instant(AchievementType.toCamel(this.achievements[0].achievementType)) + '</b><br>' +
      this.translateService.instant(AchievementType.toCamel(this.achievements[0].achievementType) + 'Description') + '<br>';
    if (this.achievements) {
      tooltipText += '<br>' + this.translateService.instant('achievementToolTipObtainedAt') + ':<br>';
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

  tournamentToolTipText(): string {
    if (!this.achievements || this.achievements.length == 0) {
      return "";
    }
    let tooltipText: string = '<b>' + this.translateService.instant(AchievementType.toCamel(this.achievements[0].achievementType)) + '</b><br>' +
      this.translateService.instant(AchievementType.toCamel(this.achievements[0].achievementType) + 'Description') + '<br>';
    if (this.achievements) {
      tooltipText += '<br>' + this.translateService.instant('achievementToolTipObtainedBy') + ':<br>';
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
          tooltipText += this.nameUtils.getDisplayName(achievement.participant);

          //End of tournament item.
          tooltipText += '</div>';
        }
      }
      tooltipText += '</div>';
    }
    return tooltipText;
  }

  updateCoordinates($event: MouseEvent): void {
    this.mouseX = $event.clientX;
    this.mouseY = $event.clientY;
    this.calculateTooltipMargin();
  }

  clearCoordinates($event: MouseEvent): void {
    this.mouseX = undefined;
    this.mouseY = undefined;
  }


  calculateTooltipMargin(): void {
    this.screenHeight = window.innerHeight;
    this.screenWidth = window.innerWidth;
    this.onLeftBorder = false;
    this.onRightBorder = false;
    if (this.mouseX! - 150 < 0) {
      this.onLeftBorder = true;
    }
    if (this.mouseX! + 150 > this.screenWidth) {
      this.onRightBorder = true;
    }
  }
}
