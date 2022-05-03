import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {MessageService} from "../../../../../services/message.service";

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

  constructor(private duelService: DuelService, private messageService: MessageService) {
  }

  ngOnInit(): void {
  }

  private setFault(fault: boolean) {
    if (fault) {
      if (this.left) {
        this.duel.competitor1Fault = fault;
      } else {
        this.duel.competitor2Fault = fault;
      }
    }
  }

  updateFault(fault: boolean) {
    let originalDuel: Duel = {...this.duel}
    this.setFault(fault);
    this.duelService.update(this.duel).subscribe(duel => {
      this.messageService.infoMessage("Fault Updated");
      return duel;
    });
    this.duel = originalDuel;
  }

  hasFault(): boolean {
    if (this.left) {
      return this.duel.competitor1Fault;
    } else {
      return this.duel.competitor2Fault;
    }
  }

}
