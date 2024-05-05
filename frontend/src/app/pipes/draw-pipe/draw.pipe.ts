import {Pipe, PipeTransform} from '@angular/core';
import {Duel} from "../../models/duel";
import {Score} from "../../models/score";

@Pipe({
  name: 'draw'
})
export class DrawPipe implements PipeTransform {

  transform(duel: Duel, ...args: unknown[]): boolean {
    return duel.finished && this.countPoints(duel.competitor1Score) === this.countPoints(duel.competitor2Score);
  }

  countPoints(scores: Score[]): number {
    let sum: number = 0;
    scores.forEach((score: Score): void => {
      if (score != Score.EMPTY) {
        sum++;
      }
    });
    return sum;
  }

}
