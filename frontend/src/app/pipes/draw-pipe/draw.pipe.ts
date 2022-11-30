import {Pipe, PipeTransform} from '@angular/core';
import {Duel} from "../../models/duel";
import {Score} from "../../models/score";

@Pipe({
  name: 'draw'
})
export class DrawPipe implements PipeTransform {

  transform(duel: Duel, ...args: unknown[]): boolean {
    if (!duel.duration) {
      return false;
    }
    return duel.duration > 0 && this.countPoints(duel.competitor1Score) === this.countPoints(duel.competitor2Score);
  }

  countPoints(scores: Score[]): number {
    let sum = 0;
    scores.forEach(score => {
      if (score != Score.EMPTY) {
        sum++;
      }
    });
    return sum;
  }

}
