import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {MessageService} from "../../../../../services/message.service";
import {Score} from "../../../../../models/score";

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

  constructor(private duelService: DuelService, private messageService: MessageService) {
  }

  ngOnInit(): void {
    // This is intentional
  }

  private setFault(fault: boolean) {
    if ((this.left && !this.swapTeams) || (!this.left && this.swapTeams)) {
      if (!fault || !this.duel.competitor1Fault) {
        this.duel.competitor1Fault = fault;
      } else {
        this.duel.competitor1Fault = false;
        if (this.duel.competitor2Score.length < 2) {
          this.duel.competitor2Score.push(Score.HANSOKU);
        }
      }
    } else {
      if (!fault || !this.duel.competitor2Fault) {
        this.duel.competitor2Fault = fault;
      } else {
        this.duel.competitor2Fault = false;
        if (this.duel.competitor1Score.length < 2) {
          this.duel.competitor1Score.push(Score.HANSOKU);
        }
      }
    }
  }

  updateFault(fault: boolean) {
    this.setFault(fault);
    this.duelService.update(this.duel).subscribe(duel => {
      this.messageService.infoMessage("Fault Updated");
      return duel;
    });
  }

}
