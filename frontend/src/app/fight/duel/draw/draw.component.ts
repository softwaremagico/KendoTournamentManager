import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../models/Duel";

@Component({
  selector: 'draw',
  templateUrl: './draw.component.html',
  styleUrls: ['./draw.component.scss']
})
export class DrawComponent implements OnInit {

  @Input()
  duel: Duel;

  constructor() { }

  ngOnInit(): void {
  }

}
