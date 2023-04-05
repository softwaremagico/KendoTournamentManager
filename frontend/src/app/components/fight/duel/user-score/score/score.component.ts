import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {Score} from "../../../../../models/score";
import {MessageService} from "../../../../../services/message.service";
import {ScoreUpdatedService} from "../../../../../services/notifications/score-updated.service";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'score',
  templateUrl: './score.component.html',
  styleUrls: ['./score.component.scss'],
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

  scoreRepresentation: string;

  constructor(private duelService: DuelService, private scoreUpdatedService: ScoreUpdatedService, private messageService: MessageService,
              private translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.scoreUpdatedService.isScoreUpdated.subscribe(duel => {
      if (duel == this.duel) {
        this.scoreRepresentation = this.getScoreRepresentation();
      }
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['duel'] || changes['left'] || changes['swapTeams']) {
      this.scoreRepresentation = this.getScoreRepresentation();
    }
  }

  private updateDuel(score: Score): boolean {
    let updated = false;
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
              this.duel.competitor1Score.splice(this.index, 1);
              this.duel.competitor1ScoreTime.splice(this.index, 1);
              updated = true;
            }
          } else {
            if (this.duel.competitor2Score[this.index] !== undefined) {
              this.duel.competitor2Score.splice(this.index, 1);
              this.duel.competitor2ScoreTime.splice(this.index, 1);
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
              this.duel.competitor2Score.splice(this.index, 1);
              this.duel.competitor2ScoreTime.splice(this.index, 1);
              updated = true;
            }
          } else {
            if (this.duel.competitor1Score[this.index] !== undefined) {
              this.duel.competitor1Score.splice(this.index, 1);
              this.duel.competitor1ScoreTime.splice(this.index, 1);
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

  updateScore(score: Score) {
    if (this.updateDuel(score)) {
      this.duel.finishedAt = undefined;
      this.duelService.update(this.duel).subscribe(duel => {
        this.messageService.infoMessage('infoScoreUpdated');
        return duel;
      });
    }
  }

  getScoreRepresentation(): string {
    if (this.left) {
      if (!this.swapTeams) {
        return Score.tag(this.duel.competitor1Score[this.index]);
      } else {
        return Score.tag(this.duel.competitor2Score[this.index]);
      }
    } else {
      if (!this.swapTeams) {
        return Score.tag(this.duel.competitor2Score[this.index]);
      } else {
        return Score.tag(this.duel.competitor1Score[this.index]);
      }
    }
  }

  possibleScores(): Score[] {
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

}
