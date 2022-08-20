import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../../models/duel";

@Component({
  selector: 'draw',
  templateUrl: './draw.component.html',
  styleUrls: ['./draw.component.scss']
})
export class DrawComponent implements OnInit {

  @Input()
  duel: Duel;

  ngOnInit(): void {
    // This is intentional
  }

  isDrawDuel() {
    return this.duel.duration && this.duel.duration > 0 && this.duel.competitor1Score.length === this.duel.competitor2Score.length;
  }

}
