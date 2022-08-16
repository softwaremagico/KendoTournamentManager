import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {AudioService} from "../../services/audio.service";

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

  @Output() onTimerFinished: EventEmitter<any> = new EventEmitter();

  minutes: number;
  seconds: number;
  private clockHandler: number;
  totalSeconds: number = 0;
  private alarmOn: boolean;


  constructor(public audioService: AudioService) {
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

  @HostListener('document:keypress', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === ' ') {
      if (this.started) {
        this.pauseTimer();
      } else {
        this.startTimer();
      }
    } else if (event.key === 'Enter') {
      this.stopTimer();
    }
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
    this.onTimerFinished.emit([this.totalSeconds]);
    this.resetVariables(this.startingMinutes, this.startingSeconds, false);
    this.alarmOn = false;
    this.totalSeconds = 0;
  };

  timerComplete() {
    this.onTimerFinished.emit([this.totalSeconds]);
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
    //Here only is launched when seconds changes from 1 to 0.
    if (this.seconds === 0 && this.minutes === 0 && !this.alarmOn) {
      this.alarmOn = true;
      this.audioService.playAlarm();
    }
    this.totalSeconds++;
    return;
  };

  isWarningTime(): boolean {
    return this.minutes == 0 && this.seconds < 30 && this.seconds > 10;
  }

  isAlmostFinished(): boolean {
    return this.minutes == 0 && this.seconds <= 10;
  }

  toDoubleDigit(num: number): string {
    return num < 10 ? '0' + num : num + '';
  };

  addTime(time: number) {
    this.seconds += time;
    const rawSeconds: number = this.seconds;
    this.seconds = this.seconds % 60;
    if (this.seconds < 0) {
      if (this.minutes > 0) {
        this.seconds = 60 + this.seconds;
      } else {
        this.seconds = 0;
      }
    }
    this.minutes += Math.floor(rawSeconds / 60);
    if (this.minutes < 0) {
      this.minutes = 0;
    } else if (this.minutes > 20) {
      this.minutes = 20;
    }
  }

}
