import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {AudioService} from "../../services/audio.service";
import {TimeChangedService} from "../../services/notifications/time-changed.service";
import {takeUntil} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {ConfirmationDialogComponent} from "../basic/confirmation-dialog/confirmation-dialog.component";
import {RbacBasedComponent} from "../RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";

@Component({
  selector: 'app-timer',
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.scss']
})
export class TimerComponent extends RbacBasedComponent implements OnInit {

  started = false;
  minutesEditable = false;
  secondsEditable = false;

  @Input()
  set startingMinutes(value: number) {
    this.minutes = value;
  }

  @Input()
  set startingSeconds(value: number) {
    this.seconds = value;
  }

  @Output() onTimerFinished: EventEmitter<any> = new EventEmitter();
  @Output() onTimerChanged: EventEmitter<any> = new EventEmitter();
  @Output() timeDurationChanged: EventEmitter<any> = new EventEmitter();
  @Output() timerClosed: EventEmitter<any> = new EventEmitter();

  minutes: number;
  seconds: number;
  private clockHandler: NodeJS.Timeout;
  elapsedSeconds: number = 0;
  private alarmOn: boolean;
  totalTime: number;
  increasedTime: number = 0;


  constructor(public audioService: AudioService, private timeChangedService: TimeChangedService, private dialog: MatDialog,
              rbacService: RbacService) {
    super(rbacService);
    this.started = false;
  }

  ngOnInit(): void {
    const self: TimerComponent = this;
    this.clockHandler = setInterval(function () {
      self.secondElapsed.apply(self);
    }, 1000);
    this.timeChangedService.isElapsedTimeChanged.pipe(takeUntil(this.destroySubject)).subscribe(elapsedTime => {
      this.elapsedSeconds = elapsedTime;
    });
    this.timeChangedService.isTotalTimeChanged.pipe(takeUntil(this.destroySubject)).subscribe(totalTime => {
      this.resetVariablesAsSeconds(totalTime, false);
      this.totalTime = totalTime;
    });
  }

  @HostListener('document:keypress', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (!this.secondsEditable && !this.minutesEditable) {
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
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardNonPrintableEvent(event: KeyboardEvent) {
    if (!this.secondsEditable && !this.minutesEditable) {
      if (event.key === 'Backspace') {
        this.restoreTimer();
      } else if (event.key === 'Escape') {
        this.timerClosed.emit(true);
      }
    }
  }

  resetVariablesAsSeconds(rawSeconds: number, started: boolean) {
    rawSeconds = rawSeconds - this.elapsedSeconds;
    if (rawSeconds < 0) {
      rawSeconds = 0;
    }
    this.seconds = rawSeconds % 60;
    this.minutes = Math.floor(rawSeconds / 60);
    this.started = started;
  }

  resetVariables(minutes: number, seconds: number, started: boolean) {
    this.resetVariablesAsSeconds(minutes * 60 + seconds, started);
  }

  startTimer() {
    this.started = true;
  };

  pauseTimer() {
    this.started = false;
    this.onTimerChanged.emit([this.elapsedSeconds]);
  };

  finishTimer() {
    if (!this.elapsedSeconds) {
      this.elapsedSeconds = 1;
    }
    this.onTimerFinished.emit([this.elapsedSeconds]);
    this.resetVariables(this.minutes, this.seconds, false);
    this.alarmOn = false;
    this.elapsedSeconds = 0;
  };

  restoreTimer() {
    if (this.elapsedSeconds === 0) {
      return;
    }
    let dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      disableClose: false
    });
    dialogRef.componentInstance.messageTag = "timerResetWarning"

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.elapsedSeconds = 0;
        this.onTimerChanged.emit([this.elapsedSeconds]);
        this.resetVariablesAsSeconds(this.totalTime + this.increasedTime, false);
        this.alarmOn = false;
      }
    });
  }

  timerComplete() {
    this.onTimerFinished.emit([this.elapsedSeconds]);
    this.started = false;
  }

  secondElapsed() {
    if (!this.started) {
      return;
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
    if (this.seconds % 3 == 0) {
      this.onTimerChanged.emit([this.elapsedSeconds]);
    }
  }

  isWarningTime(): boolean {
    return this.minutes == 0 && this.seconds < 30 && this.seconds > 10;
  }

  isAlmostFinished(): boolean {
    return this.minutes == 0 && this.seconds <= 10;
  }

  toDoubleDigit(num: number): string {
    if (isNaN(num)) {
      return '00';
    }
    return num < 10 ? '0' + num : num + '';
  };

  addTime(time: number) {
    this.seconds += time;
    this.increasedTime += time;
    let rawSeconds: number = this.seconds + this.minutes * 60;
    if (rawSeconds < 0) {
      rawSeconds = 0;
    }
    this.seconds = rawSeconds % 60;
    this.minutes = Math.floor(rawSeconds / 60);
    if (this.minutes > 20) {
      this.minutes = 20;
    }
    this.timeDurationChanged.emit([rawSeconds + this.elapsedSeconds]);
    this.onTimerChanged.emit([this.elapsedSeconds]);
  }

  setMinutesEditable(editable: boolean) {
    this.minutesEditable = editable;
    this.secondsEditable = false;
  }

  setSecondsEditable(editable: boolean) {
    this.secondsEditable = editable;
    this.minutesEditable = false;
  }

  @HostListener('document:click', ['$event.target'])
  onClick(element: HTMLElement) {
    if (!element.classList.contains('timer-edition-minutes')) {
      this.minutesEditable = false;
    }
    if (!element.classList.contains('timer-edition-seconds')) {
      this.secondsEditable = false;
    }
  }

  validateInputMinutes(event: Event) {
    let inputValue: number = Number((event.target as HTMLInputElement).value);
    if (isNaN(inputValue)) {
      inputValue = this.minutes;
    } else if (inputValue < 0) {
      inputValue = 0;
    } else if (inputValue > 20) {
      inputValue = 20;
    }
    this.minutes = inputValue;
    this.onTimerChanged.emit([this.elapsedSeconds]);
  }

  validateInputSeconds(event: Event) {
    let inputValue: number = Number((event.target as HTMLInputElement).value);
    if (isNaN(inputValue)) {
      inputValue = this.seconds;
    } else if (inputValue < 0) {
      inputValue = 0;
    } else if (inputValue > 59) {
      inputValue = 59;
    }
    this.seconds = inputValue;
    this.onTimerChanged.emit([this.elapsedSeconds]);
  }
}
