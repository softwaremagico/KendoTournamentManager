import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../models/Duel";

@Component({
  selector: 'app-duel',
  templateUrl: './duel.component.html',
  styleUrls: ['./duel.component.scss']
})
export class DuelComponent implements OnInit {

  constructor() { }

  @Input()
  duel: Duel;

  ngOnInit(): void {
  }

}
