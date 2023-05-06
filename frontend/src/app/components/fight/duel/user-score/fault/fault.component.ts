import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
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
export class FaultComponent implements OnInit, OnChanges {

  @Input()
  duel: Duel;

  @Input()
  left: boolean;

  @Input()
  swapTeams: boolean;

  timeRepresentation: string | undefined;

  constructor(private duelService: DuelService, private scoreUpdatedService: ScoreUpdatedService, private messageService: MessageService) {
  }

  ngOnInit(): void {
    // This is intentional
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['duel'] || changes['left'] || changes['swapTeams']) {
      this.setTime();
    }
  }

  private setFault(fault: boolean): boolean {
    if ((this.left && !this.swapTeams) || (!this.left && this.swapTeams)) {
      if (!fault || !this.duel.competitor1Fault) {
        this.duel.competitor1Fault = fault;
        if (fault) {
          this.duel.competitor1FaultTime = this.duel.duration!;
        } else {
          this.duel.competitor1FaultTime = undefined;
        }
      } else {
        this.duel.competitor1Fault = false;
        this.duel.competitor1FaultTime = undefined;
        if (this.duel.competitor2Score.length < 2) {
          this.duel.competitor2Score.push(Score.HANSOKU);
          this.duel.competitor2ScoreTime.push(this.duel.duration!);
          this.scoreUpdatedService.isScoreUpdated.next(this.duel);
        } else {
          this.messageService.warningMessage("scoreNotAdded");
          this.setTime();
          return false;
        }
      }
    } else {
      if (!fault || !this.duel.competitor2Fault) {
        this.duel.competitor2Fault = fault;
        if (fault) {
          this.duel.competitor2FaultTime = this.duel.duration!;
        } else {
          this.duel.competitor2FaultTime = undefined;
        }
      } else {
        this.duel.competitor2Fault = false;
        this.duel.competitor2FaultTime = undefined;
        if (this.duel.competitor1Score.length < 2) {
          this.duel.competitor1Score.push(Score.HANSOKU);
          this.duel.competitor1ScoreTime.push(this.duel.duration!);
          this.scoreUpdatedService.isScoreUpdated.next(this.duel);
        } else {
          this.messageService.warningMessage("scoreNotAdded");
          this.setTime();
          return false;
        }
      }
    }
    this.setTime();
    return true;
  }

  updateFault(fault: boolean) {
    const faultAdded: boolean = this.setFault(fault);
    this.duelService.update(this.duel).subscribe(duel => {
      if (faultAdded) {
        this.messageService.infoMessage('infoFaultUpdated');
      }
      return duel;
    });
  }

  setTime() {
    let seconds: number | undefined = (this.left && !this.swapTeams) || (!this.left && this.swapTeams) ?
      this.duel.competitor1FaultTime : this.duel.competitor2FaultTime;
    if (seconds) {
      const minutes: number | undefined = seconds ? Math.floor(seconds / 60) : undefined;
      seconds = seconds % 60;
      let text: string = "";
      if (minutes) {
        text += minutes + "' ";
      }
      if (seconds) {
        text += seconds + '"';
      }
      this.timeRepresentation = text;
    } else {
      this.timeRepresentation = undefined;
    }
  }
}
