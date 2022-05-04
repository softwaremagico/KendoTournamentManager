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

  unsorted = (a: KeyValue<number, string>, b: KeyValue<number, string>): number => {
    return 0;
  }

  constructor(private duelService: DuelService, private messageService: MessageService) {
  }

  ngOnInit(): void {
  }

  private updateDuel(score: Score) {
    if (score) {
      if (this.left) {
        this.duel.competitor1Score[this.index] = score;
      } else {
        this.duel.competitor2Score[this.index] = score;
      }
    }
  }

  updateScore(score: Score) {
    //let originalDuel: Duel = Duel.clone(this.duel);
    this.updateDuel(score);
    this.duelService.update(this.duel).subscribe(duel => {
      this.messageService.infoMessage("Score Updated");
      return duel;
    });
    //this.duel = Duel.clone(originalDuel);
  }

  scoreRepresentation(): string {
    if (this.left) {
      return Score.tag(this.duel.competitor1Score[this.index]);
    } else {
      return Score.tag(this.duel.competitor2Score[this.index]);
    }
  }

}
