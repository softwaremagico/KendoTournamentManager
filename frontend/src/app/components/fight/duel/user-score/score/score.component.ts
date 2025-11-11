import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewEncapsulation} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {Score} from "../../../../../models/score";
import {MessageService} from "../../../../../services/message.service";
import {ScoreUpdatedService} from "../../../../../services/notifications/score-updated.service";
import {TranslateService} from "@ngx-translate/core";
import {RbacService} from "../../../../../services/rbac/rbac.service";
import {RbacActivity} from "../../../../../services/rbac/rbac.activity";

@Component({
  selector: 'score',
  templateUrl: './score.component.html',
  styleUrls: ['./score.component.scss'],
  // tooltip style not applied without this:
  encapsulation: ViewEncapsulation.None,
})
export class ScoreComponent implements OnInit, OnChanges {

  @Input()
  index: number;

  @Input()
  duel: Duel;

  @Input()
  left: boolean;

  @Input()
  swapTeams: boolean;

  @Input()
  locked: boolean = true;

  scoreRepresentation: string;

  timeRepresentation: string | undefined;

  mouseX: number | undefined;
  mouseY: number | undefined;
  screenHeight: number | undefined;
  screenWidth: number | undefined;
  onLeftBorder: boolean;
  onRightBorder: boolean;

  protected readonly RbacActivity = RbacActivity;
  protected readonly Score = Score;

  constructor(private duelService: DuelService, private scoreUpdatedService: ScoreUpdatedService, private messageService: MessageService,
              private translateService: TranslateService, public rbacService: RbacService) {
  }

  ngOnInit(): void {
    this.scoreUpdatedService.isScoreUpdated.subscribe((duel: Duel): void => {
      if (duel == this.duel) {
        this.scoreRepresentation = this.getScoreRepresentation();
        this.setTime();
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['duel'] || changes['left'] || changes['swapTeams']) {
      this.scoreRepresentation = this.getScoreRepresentation();
      this.setTime();
    }
  }

  private updateDuel(score: Score): boolean {
    let updated: boolean = false;
    if (score) {
      if (this.left) {
        if (score !== Score.EMPTY) {
          if (!this.swapTeams) {
            if (this.duel.competitor1Score[this.index] !== score) {
              this.duel.competitor1Score[this.index] = score;
              this.duel.competitor1ScoreTime[this.index] = this.duel.duration!;
              updated = true;
            }
          } else {
            if (this.duel.competitor2Score[this.index] !== score) {
              this.duel.competitor2Score[this.index] = score;
              this.duel.competitor2ScoreTime[this.index] = this.duel.duration!;
              updated = true;
            }
          }
        } else {
          if (!this.swapTeams) {
            if (this.duel.competitor1Score[this.index] !== undefined) {
              delete this.duel.competitor1Score[this.index];
              delete this.duel.competitor1ScoreTime[this.index];
              updated = true;
            }
          } else {
            if (this.duel.competitor2Score[this.index] !== undefined) {
              delete this.duel.competitor2Score[this.index];
              delete this.duel.competitor2ScoreTime[this.index];
              updated = true;
            }
          }
        }
      } else {
        if (score !== Score.EMPTY) {
          if (!this.swapTeams) {
            if (this.duel.competitor2Score[this.index] !== score) {
              this.duel.competitor2Score[this.index] = score;
              this.duel.competitor2ScoreTime[this.index] = this.duel.duration!;
              updated = true;
            }
          } else {
            if (this.duel.competitor1Score[this.index] !== score) {
              this.duel.competitor1Score[this.index] = score;
              this.duel.competitor1ScoreTime[this.index] = this.duel.duration!;
              updated = true;
            }
          }
        } else {
          if (!this.swapTeams) {
            if (this.duel.competitor2Score[this.index] !== undefined) {
              delete this.duel.competitor2Score[this.index];
              delete this.duel.competitor2ScoreTime[this.index];
              updated = true;
            }
          } else {
            if (this.duel.competitor1Score[this.index] !== undefined) {
              delete this.duel.competitor1Score[this.index];
              delete this.duel.competitor1ScoreTime[this.index];
              updated = true;
            }
          }
        }
      }
    }
    if (updated) {
      this.scoreUpdatedService.isScoreUpdated.next(this.duel);
    }
    return updated;
  }

  updateScore(score: Score): void {
    if (this.updateDuel(score)) {
      this.duel.finishedAt = undefined;
      this.duelService.update(this.duel).subscribe((duel: Duel): Duel => {
        this.messageService.infoMessage('infoScoreUpdated');
        return duel;
      });
    }
  }

  getScoreRepresentation(): string {
    return Score.tag(this.getScore());
  }

  getScore(): Score {
    if (this.left) {
      if (!this.swapTeams) {
        return this.duel.competitor1Score[this.index];
      } else {
        return this.duel.competitor2Score[this.index];
      }
    } else {
      if (!this.swapTeams) {
        return this.duel.competitor2Score[this.index];
      } else {
        return this.duel.competitor1Score[this.index];
      }
    }
  }

  possibleScores(): Score[] {
    if (this.locked) {
      return [];
    }
    if (this.left) {
      if (!this.swapTeams) {
        if (!this.duel.competitor1) {
          return Score.clear();
        }
        if (!this.duel.competitor2) {
          return Score.noCompetitor();
        }
      } else {
        if (!this.duel.competitor2) {
          return Score.clear();
        }
        if (!this.duel.competitor1) {
          return Score.noCompetitor();
        }
      }
    } else {
      if (!this.swapTeams) {
        if (!this.duel.competitor2) {
          return Score.clear();
        }
        if (!this.duel.competitor1) {
          return Score.noCompetitor();
        }
      } else {
        if (!this.duel.competitor1) {
          return Score.clear();
        }
        if (!this.duel.competitor2) {
          return Score.noCompetitor();
        }
      }
    }
    return Score.toArray();
  }

  setTime(): void {
    let seconds: number | undefined = (this.left && !this.swapTeams) || (!this.left && this.swapTeams) ?
      this.duel.competitor1ScoreTime[this.index] : this.duel.competitor2ScoreTime[this.index];
    if (seconds) {
      const minutes: number | undefined = seconds ? Math.floor(seconds / 60) : undefined;
      seconds = seconds % 60;
      let text: string = "";
      if (minutes) {
        text += minutes + " " + this.translateService.instant('minutesAbbreviation') + " ";
      }
      if (seconds) {
        text += seconds + " " + this.translateService.instant('secondsAbbreviation') + " ";
      }
      this.timeRepresentation = text;
    } else {
      this.timeRepresentation = undefined;
    }
  }

  tooltipText(): string {
    if (!this.timeRepresentation || this.timeRepresentation.length == 0) {
      return "";
    }
    return '<span class="tooltip-score"><b>' + this.getScore() + '</b></span><br>' +
      '<div class="time-tooltip-container"><span class="material-icons time-tooltip">timer</span><span class="time-tooltip-text">' + this.timeRepresentation + '</span></div>';
  }

  updateCoordinates($event: MouseEvent): void {
    this.mouseX = $event.clientX;
    this.mouseY = $event.clientY;
    this.calculateTooltipMargin();
  }

  clearCoordinates(): void {
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
