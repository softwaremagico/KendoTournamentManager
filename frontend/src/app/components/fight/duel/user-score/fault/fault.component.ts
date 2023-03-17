import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {MessageService} from "../../../../../services/message.service";
import {Score} from "../../../../../models/score";
import {ScoreUpdatedService} from "../../../../../services/notifications/score-updated.service";

@Component({
  selector: 'fault',
  templateUrl: './fault.component.html',
  styleUrls: ['./fault.component.scss']
})
export class FaultComponent implements OnInit {

  @Input()
  duel: Duel;

  @Input()
  left: boolean;

  @Input()
  swapTeams: boolean;

  constructor(private duelService: DuelService, private scoreUpdatedService: ScoreUpdatedService, private messageService: MessageService) {
  }

  ngOnInit(): void {
    // This is intentional
  }

  private setFault(fault: boolean): boolean {
    if ((this.left && !this.swapTeams) || (!this.left && this.swapTeams)) {
      if (!fault || !this.duel.competitor1Fault) {
        this.duel.competitor1Fault = fault;
        this.duel.competitor1FaultTime = this.duel.duration!;
      } else {
        this.duel.competitor1Fault = false;
        if (this.duel.competitor2Score.length < 2) {
          this.duel.competitor2Score.push(Score.HANSOKU);
          this.duel.competitor2ScoreTime.push(this.duel.duration!);
          this.scoreUpdatedService.isScoreUpdated.next(this.duel);
        } else {
          this.messageService.warningMessage("scoreNotAdded");
          return false;
        }
      }
    } else {
      if (!fault || !this.duel.competitor2Fault) {
        this.duel.competitor2Fault = fault;
        this.duel.competitor2FaultTime = this.duel.duration!;
      } else {
        this.duel.competitor2Fault = false;
        if (this.duel.competitor1Score.length < 2) {
          this.duel.competitor1Score.push(Score.HANSOKU);
          this.duel.competitor1ScoreTime.push(this.duel.duration!);
          this.scoreUpdatedService.isScoreUpdated.next(this.duel);
        } else {
          this.messageService.warningMessage("scoreNotAdded");
          return false;
        }
      }
    }
    return true;
  }

  updateFault(fault: boolean) {
    const faultAdded: boolean = this.setFault(fault);
    this.duel.finishedAt = undefined;
    this.duelService.update(this.duel).subscribe(duel => {
      if (faultAdded) {
        this.messageService.infoMessage('infoFaultUpdated');
      }
      return duel;
    });
  }

}
