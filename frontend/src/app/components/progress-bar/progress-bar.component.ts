import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'app-progress-bar',
  templateUrl: './progress-bar.component.html',
  styleUrls: ['./progress-bar.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class ProgressBarComponent implements OnInit {

  intervalId: NodeJS.Timeout;

  drawnPercentage: number = 0;

  @Input()
  text: string | undefined = undefined;

  @Input()
  percentage: number = 50;

  @Input()
  barIcon: string = "attack";

  progressInLoading() {
    if (this.drawnPercentage === 100) {
      clearInterval(this.intervalId);
    }
  }

  ngOnInit(): void {
    this.intervalId = setInterval(() => {
      if (this.drawnPercentage < this.percentage) {
        this.drawnPercentage += 1;
      }
    }, 50);
  }
}
