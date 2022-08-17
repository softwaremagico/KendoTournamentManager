import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {AudioService} from "../../services/audio.service";
import {DuelChangedService} from "../../services/duel-changed.service";

@Component({
  selector: 'app-timer',
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.scss']
})
export class TimerComponent implements OnInit {

  started = false;

  @Input()
  set startingMinutes(value: number) {
    this.minutes = value;
  }

  @Input()
  set startingSeconds(value: number) {
    this.seconds = value;
  }

  @Output() onTimerFinished: EventEmitter<any> = new EventEmitter();
  @Output() onTimerPaused: EventEmitter<any> = new EventEmitter();
  @Output() timeDurationChanged: EventEmitter<any> = new EventEmitter();

  minutes: number;
  seconds: number;
  private clockHandler: number;
  elapsedSeconds: number = 0;
  private alarmOn: boolean;


  constructor(public audioService: AudioService, private duelChangedService: DuelChangedService) {
    this.started = false;
    this.duelChangedService.isDuelSelected.subscribe(selectedDuel => {
      if (selectedDuel.duration) {
        this.elapsedSeconds = selectedDuel.duration;
        if (selectedDuel.totalDuration) {
          this.resetVariablesAsSeconds(selectedDuel.totalDuration - selectedDuel.duration, false);
        }
      } else {
        if (selectedDuel.totalDuration) {
          this.resetVariablesAsSeconds(selectedDuel.totalDuration, false);
        }
      }
    });
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
      this.finishTimer();
    }
  }

  resetVariablesAsSeconds(rawSeconds: number, started: boolean) {
    this.seconds = rawSeconds % 60;
    this.minutes = Math.floor(rawSeconds / 60);
    this.started = started;
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
    this.onTimerPaused.emit([this.elapsedSeconds]);
  };

  finishTimer() {
    if (!this.elapsedSeconds) {
      this.elapsedSeconds = 1;
    }
    this.onTimerFinished.emit([this.elapsedSeconds]);
    this.resetVariables(this.startingMinutes, this.startingSeconds, false);
    this.alarmOn = false;
    this.elapsedSeconds = 0;
  };

  timerComplete() {
    this.onTimerFinished.emit([this.elapsedSeconds]);
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
    this.elapsedSeconds++;
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
    this.elapsedSeconds += time;
    const rawSeconds: number = this.seconds + this.minutes * 60;
    this.seconds = rawSeconds % 60;
    this.minutes = Math.floor(rawSeconds / 60);
    if (this.minutes < 0) {
      this.minutes = 0;
    } else if (this.minutes > 20) {
      this.minutes = 20;
    }
    this.timeDurationChanged.emit([(this.seconds + this.minutes * 60)]);
  }

}
