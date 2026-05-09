import {Component, Input} from '@angular/core';
import {Duel} from "../../../../models/duel";

@Component({
  selector: 'draw',
  templateUrl: './draw.component.html',
  styleUrls: ['./draw.component.scss']
})
export class DrawComponent {

  @Input()
  duel: Duel;

}
