import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../models/duel";

@Component({
  selector: 'duel',
  templateUrl: './duel.component.html',
  styleUrls: ['./duel.component.scss']
})
export class DuelComponent implements OnInit {

  @Input()
  duel: Duel;

  constructor() { }

  ngOnInit(): void {
  }

}
