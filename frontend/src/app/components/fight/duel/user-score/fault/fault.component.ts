import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../../../models/Duel";

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

  constructor() {
  }

  ngOnInit(): void {
  }

}
