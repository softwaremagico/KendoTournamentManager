import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {Score} from "../../../../../models/score";
import {MessageService} from "../../../../../services/message.service";
import {KeyValue} from "@angular/common";

@Component({
  selector: 'score',
  templateUrl: './score.component.html',
  styleUrls: ['./score.component.scss']
})
export class ScoreComponent implements OnInit {

  @Input()
  index: number;

  @Input()
  duel: Duel;

  @Input()
  left: boolean;

  public scores = Score;

  get ScoreEnum(): typeof Score {
    return Score;
  }

  unsorted = (_a: KeyValue<number, string>, _b: KeyValue<number, string>): number => {
    return 0;
  }

  constructor(private duelService: DuelService, private messageService: MessageService) {
  }

  ngOnInit(): void {
    // This is intentional
  }

  private updateDuel(score: Score) {
    if (score) {
      if (this.left) {
        if (score !== Score.EMPTY) {
          this.duel.competitor1Score[this.index] = score;
        } else {
          this.duel.competitor1Score.splice(this.index, 1)
        }
      } else {
        if (score !== Score.EMPTY) {
          this.duel.competitor2Score[this.index] = score;
        } else {
          this.duel.competitor2Score.splice(this.index, 1)
        }
      }
    }
  }

  updateScore(score: Score) {
    this.updateDuel(score);
    this.duelService.update(this.duel).subscribe(duel => {
      this.messageService.infoMessage("Score Updated");
      return duel;
    });
  }

  scoreRepresentation(): string {
    if (this.left) {
      return Score.tag(this.duel.competitor1Score[this.index]);
    } else {
      return Score.tag(this.duel.competitor2Score[this.index]);
    }
  }

}
