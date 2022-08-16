import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-timer',
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.scss']
})
export class TimerComponent implements OnInit {

  started = false;

  @Input()
  startingMinutes: number = 2;

  @Input()
  startingSeconds: number = 0;

  minutes: number;
  seconds: number;
  private clockHandler: number;

  constructor() {
    this.started = false;
    this.minutes = this.startingMinutes;
    this.seconds = this.startingSeconds;
  }

  ngOnInit(): void {
    const self: TimerComponent = this;
    this.clockHandler = setInterval(function () {
      self.secondElapsed.apply(self);
    }, 1000);
  }

  resetVariables(mins: number, secs: number, started: boolean) {
    this.minutes = mins;
    this.seconds = secs;
    this.started = started;
  }

  startTimer() {
    this.started = true;
  };

  pauseTimer() {
    this.started = false;
  };

  stopTimer() {
    this.resetVariables(this.startingMinutes, this.startingSeconds, false);
  };

  timerComplete() {
    this.started = false;
  }

  secondElapsed() {
    if (!this.started) {
      return false;
    }
    if (this.seconds === 0) {
      if (this.minutes === 0) {
        this.timerComplete();
        return;
      }
      this.seconds = 59;
      this.minutes--;
    } else {
      this.seconds--;
    }
    return;
  };

  toDoubleDigit(num: number): string {
    return num < 10 ? '0' + num : num + '';
  };

}
