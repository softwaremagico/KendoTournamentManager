import {Component, Input, OnInit, SimpleChanges} from '@angular/core';

@Component({
  selector: 'app-arrow',
  templateUrl: './arrow.component.html',
  styleUrls: ['./arrow.component.scss']
})
export class ArrowComponent implements OnInit {

  public static readonly ARROW_SIZE: number = 30;
  arrow_size: number = ArrowComponent.ARROW_SIZE;

  @Input() x1: number;
  @Input() y1: number;
  @Input() x2: number;
  @Input() y2: number;

  @Input() color: string = "#ff00ff";

  height: number;
  width: number;

  constructor() {
  }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['x1'] || changes['x2'] || changes['y1'] || changes['y2']) {
      this.height = Math.max(this.y2, this.y1) + ArrowComponent.ARROW_SIZE / 2;
      this.width = Math.max(this.x2, this.x1);
    }
  }

}
