import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {AudioService} from "../../services/audio.service";
import {TimeChangedService} from "../../services/notifications/time-changed.service";
import {Subject, takeUntil} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {ConfirmationDialogComponent} from "../basic/confirmation-dialog/confirmation-dialog.component";
import {RbacBasedComponent} from "../RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {CdkDragEnd, Point} from "@angular/cdk/drag-drop";

@Component({
  selector: 'app-timer',
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.scss']
})
export class TimerComponent extends RbacBasedComponent implements OnInit {

  private TIMER_HEIGHT: number = 320;

  @Input()
  set startingMinutes(value: number) {
    this.minutes = value;
  }

  @Input()
  editable: boolean = true;

  @Input()
  set startingSeconds(value: number) {
    this.seconds = value;
  }

  @Input()
  resetTimerPosition: Subject<boolean>;

  @Output() onTimerFinished: EventEmitter<boolean[]> = new EventEmitter();
  @Output() onTimerChanged: EventEmitter<any> = new EventEmitter();
  @Output() onSoftTimerChanged: EventEmitter<any> = new EventEmitter();
  @Output() onPlayPressed: EventEmitter<any> = new EventEmitter();
  @Output() timeDurationChanged: EventEmitter<any> = new EventEmitter();
  @Output() timerClosed: EventEmitter<any> = new EventEmitter();

  minutes: number;
  seconds: number;
  private clockHandler: NodeJS.Timeout | null;
  elapsedSeconds: number = 0;
  private alarmOn: boolean;
  totalTime: number;
  increasedTime: number = 0;
  started: boolean = false;
  minutesEditable: boolean = false;
  secondsEditable: boolean = false;
  private clickedElement: HTMLElement;

  timerPosition: Point = {x: 0, y: 0};
  private screenWidth: number;
  private screenHeight: number;


  constructor(public audioService: AudioService, private timeChangedService: TimeChangedService, private dialog: MatDialog,
              rbacService: RbacService) {
    super(rbacService);
    this.started = false;
  }

  ngOnInit(): void {
    this.screenWidth = window.innerWidth;
    this.screenHeight = window.innerHeight;

    const self: TimerComponent = this;
    this.clockHandler = setInterval(function (): void {
      self.secondElapsed.apply(self);
    }, 1000);
    this.timeChangedService.isElapsedTimeChanged.pipe(takeUntil(this.destroySubject)).subscribe(elapsedTime => {
      this.elapsedSeconds = elapsedTime;
    });
    this.timeChangedService.isTotalTimeChanged.pipe(takeUntil(this.destroySubject)).subscribe(totalTime => {
      this.resetVariablesAsSeconds(totalTime, false);
      this.totalTime = totalTime;
    });

    this.resetTimerPosition.subscribe(() => this.timerPosition = {x: 0, y: 0});
  }

  override ngOnDestroy(): void {
    if (this.clockHandler != null) {
      clearInterval(this.clockHandler);
      this.clockHandler = null;
    }
  }

  @HostListener('window:resize', ['$event'])
  onWindowResize() {
    this.screenWidth = window.innerWidth;
    this.screenHeight = window.innerHeight;
    this.correctTimerPosition();
  }

  @HostListener('document:keypress', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (!this.secondsEditable && !this.minutesEditable && this.editable) {
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
    if (!this.secondsEditable && !this.minutesEditable && this.editable) {
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
    this.onPlayPressed.emit([this.elapsedSeconds]);
  };

  pauseTimer() {
    this.started = false;
    this.onTimerChanged.emit([this.elapsedSeconds]);
  };

  finishTimer() {
    if (!this.elapsedSeconds) {
      this.elapsedSeconds = 1;
    }
    this.onTimerFinished.emit([true]);
    this.resetVariables(this.minutes, this.seconds, false);
    this.alarmOn = false;
    this.elapsedSeconds = 0;
    //Removing focus from the button for finishing timer, or space key will also finish the duel.
    if (document.activeElement) {
      (document.activeElement as HTMLElement).blur();
    }
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
    this.onTimerFinished.emit([true]);
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
      this.pauseTimer();
    }
    this.elapsedSeconds++;
    if (this.seconds % 3 == 0) {
      this.onTimerChanged.emit([this.elapsedSeconds]);
    } else {
      this.onSoftTimerChanged.emit([this.elapsedSeconds]);
    }
  }

  isWarningTime(): boolean {
    return this.minutes == 0 && this.seconds < 30 && this.seconds > 10;
  }

  isAlmostFinished(): boolean {
    return this.minutes == 0 && this.seconds <= 10;
  }

  isPaused(): boolean {
    return !this.started && this.elapsedSeconds > 0;
  }

  toDoubleDigit(num: number): string {
    if (isNaN(num)) {
      return '00';
    }
    return num < 10 ? '0' + num : num + '';
  };

  addTime(time: number): void {
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
    this.alarmOn = false;
  }

  setMinutesEditable(editable: boolean): void {
    if (editable) {
      this.pauseTimer();
    }
    this.minutesEditable = editable && this.editable;
    this.secondsEditable = false;
  }

  setSecondsEditable(editable: boolean): void {
    if (editable) {
      this.pauseTimer();
    }
    this.secondsEditable = editable && this.editable;
    this.minutesEditable = false;
  }

  @HostListener('document:click', ['$event.target'])
  onClick(element: HTMLElement): void {
    if (this.minutesEditable && !element.classList.contains('timer-edition-minutes')) {
      this.minutesEditable = false;
      this.validateMinutesElement(this.clickedElement);
    }
    if (this.secondsEditable && !element.classList.contains('timer-edition-seconds')) {
      this.secondsEditable = false;
      this.validateSecondsElement(this.clickedElement);
    }
    this.clickedElement = element;
  }

  validateInputMinutes(event: Event): void {
    this.validateMinutesElement(event.target as HTMLInputElement);
  }

  validateMinutesElement(element: HTMLElement): void {
    let inputValue: number = Number((element as HTMLInputElement).value);
    if (isNaN(inputValue)) {
      inputValue = this.minutes;
    } else if (inputValue < 0) {
      inputValue = 0;
    } else if (inputValue > 20) {
      inputValue = 20;
    }
    this.minutes = inputValue;
    this.updateElapsedTime();
  }

  validateInputSeconds(event: Event): void {
    this.validateSecondsElement(event.target as HTMLInputElement);
  }

  validateSecondsElement(element: HTMLElement): void {
    let inputValue: number = Number((element as HTMLInputElement).value);
    if (isNaN(inputValue)) {
      inputValue = this.seconds;
    } else if (inputValue < 0) {
      inputValue = 0;
    } else if (inputValue > 59) {
      inputValue = 59;
    }
    this.seconds = inputValue;
    this.updateElapsedTime();
  }

  updateElapsedTime(): void {
    this.elapsedSeconds = (this.totalTime + this.increasedTime - this.minutes * 60 - this.seconds);
    this.onTimerChanged.emit([this.elapsedSeconds]);
  }

  dragEnd($event: CdkDragEnd) {
    this.timerPosition = $event.source.getFreeDragPosition();
    this.correctTimerPosition();
  }

  correctTimerPosition() {
    if (this.timerPosition.x < -this.screenWidth / 2) {
      this.timerPosition.x = -this.screenWidth / 2;
    }
    if (this.timerPosition.x > 100) {
      this.timerPosition.x = 100;
    }
    if (this.timerPosition.y < -130) {
      this.timerPosition.y = -130;
    }
    if (this.timerPosition.y > -130 + this.screenHeight - this.TIMER_HEIGHT) {
      this.timerPosition.y = -130 + this.screenHeight - this.TIMER_HEIGHT;
    }
  }
}
