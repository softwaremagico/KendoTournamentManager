import {Component, Input} from '@angular/core';
import {Duel} from "../../../../models/duel";
import {DuelType} from "../../../../models/duel-type";
import {Participant} from "../../../../models/participant";

@Component({
  selector: 'user-score',
  templateUrl: './user-score.component.html',
  styleUrls: ['./user-score.component.scss']
})
export class UserScoreComponent {

  @Input()
  duel: Duel;

  @Input()
  duelIndex: number;

  @Input()
  left: boolean;

  @Input()
  swapTeams: boolean;

  @Input()
  showAvatar: boolean = false;

  @Input()
  locked: boolean = false;

  @Input()
  highlightedParticipantId: number | undefined;

  isUntie(): boolean {
    return this.duel.type === DuelType.UNDRAW;
  }

  getParticipant(): Participant | undefined {
    if (this.left) {
      if (!this.swapTeams) {
        return this.duel.competitor1;
      } else {
        return this.duel.competitor2;
      }
    } else {
      if (!this.swapTeams) {
        return this.duel.competitor2;
      } else {
        return this.duel.competitor1;
      }
    }
  }

}
