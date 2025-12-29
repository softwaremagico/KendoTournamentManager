import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {AudioService} from "../../services/audio.service";
import {TimeChangedService} from "../../services/notifications/time-changed.service";
import {Subject, takeUntil} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {RbacBasedComponent} from "../RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {CdkDragEnd, Point} from "@angular/cdk/drag-drop";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {FilterFocusService} from "../../services/notifications/filter-focus.service";

@Component({
  selector: 'popup-timer',
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.scss']
})
export class TimerComponent extends RbacBasedComponent implements OnInit {

  @Input()
  set startingMinutes(value: number) {
    this.minutes = value;
  }

  @Input()
  editable: boolean = true;

  @Input()
  shown: boolean = false;

  private keysControls: boolean;

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
  private alarmRinging: boolean;
  totalTime: number;
  increasedTime: number = 0;
  started: boolean = false;
  minutesEditable: boolean = false;
  secondsEditable: boolean = false;
  private clickedElement: HTMLElement;

  timerPosition: Point = {x: 0, y: 0};

  protected confirmReset: boolean = false;


  constructor(public audioService: AudioService, private timeChangedService: TimeChangedService, private dialog: MatDialog,
              rbacService: RbacService, private filterFocusService: FilterFocusService) {
    super(rbacService);
    this.started = false;
  }

  ngOnInit(): void {

    //Enable/Disable key controls if the filter is in use.
    this.filterFocusService.isFilterActive.subscribe((_value: boolean): void => {
      this.keysControls = !_value;
    })

    const self: TimerComponent = this;
    this.clockHandler = setInterval(function (): void {
      self.secondElapsed.apply(self);
    }, 1000);
    this.timeChangedService.isElapsedTimeChanged.pipe(takeUntil(this.destroySubject)).subscribe((elapsedTime: number): void => {
      this.elapsedSeconds = elapsedTime;
    });
    this.timeChangedService.isTotalTimeChanged.pipe(takeUntil(this.destroySubject)).subscribe((totalTime: number): void => {
      this.resetVariablesAsSeconds(totalTime, false);
      this.totalTime = totalTime;
    });

    this.resetTimerPosition.subscribe(() => this.timerPosition = {x: 0, y: 0});

    this.editable = this.editable && this.rbacService.isAllowed(RbacActivity.EDIT_FIGHT_TIME);
  }

  override ngOnDestroy(): void {
    if (this.clockHandler != null) {
      clearInterval(this.clockHandler);
      this.clockHandler = null;
    }
  }


  @HostListener('document:keypress', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent): void {
    if (this.shown && !this.secondsEditable && !this.minutesEditable && this.editable && this.keysControls) {
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
  handleKeyboardNonPrintableEvent(event: KeyboardEvent): void {
    if (!this.secondsEditable && !this.minutesEditable && this.editable && this.keysControls) {
      if (event.key === 'Home' || event.key === 'Backspace') {
        this.restoreTimer();
      } else if (event.key === 'Escape') {
        this.timerClosed.emit(true);
      }
    }
  }


  resetVariablesAsSeconds(rawSeconds: number, started: boolean): void {
    rawSeconds = rawSeconds - this.elapsedSeconds;
    if (rawSeconds < 0) {
      rawSeconds = 0;
    }
    this.seconds = rawSeconds % 60;
    this.minutes = Math.floor(rawSeconds / 60);
    this.started = started;
  }

  resetVariables(minutes: number, seconds: number, started: boolean): void {
    this.resetVariablesAsSeconds(minutes * 60 + seconds, started);
  }

  startTimer(): void {
    this.started = true;
    this.onPlayPressed.emit([this.elapsedSeconds]);
    this.alarmRinging = false;
  };

  pauseTimer(): void {
    this.started = false;
    this.onTimerChanged.emit([this.elapsedSeconds]);
  };

  finishTimer() {
    if (!this.elapsedSeconds) {
      this.elapsedSeconds = 1;
    }
    this.onTimerFinished.emit([true]);
    this.resetVariables(this.minutes, this.seconds, false);
    this.alarmRinging = false;
    this.elapsedSeconds = 0;
    //Removing focus from the button for finishing timer, or space key will also finish the duel.
    if (document.activeElement) {
      (document.activeElement as HTMLElement).blur();
    }
  };

  restoreTimer(): void {
    this.elapsedSeconds = 0;
    this.onTimerChanged.emit([this.elapsedSeconds]);
    this.resetVariablesAsSeconds(this.totalTime + this.increasedTime, false);
    this.alarmRinging = false;
  }

  timerComplete(): void {
    this.onTimerFinished.emit([true]);
    this.started = false;
  }

  secondElapsed(): void {
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
    if (this.seconds === 0 && this.minutes === 0 && !this.alarmRinging) {
      this.alarmRinging = true;
      this.audioService.playWhistleByTime(4);
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
    this.alarmRinging = false;
  }

  setMinutesEditable(editable: boolean): void {
    if (this.rbacService.isAllowed(RbacActivity.EDIT_FIGHT_TIME)) {
      if (editable) {
        this.pauseTimer();
      }
      this.minutesEditable = editable && this.editable;
      this.secondsEditable = false;
    }
  }

  setSecondsEditable(editable: boolean): void {
    if (this.rbacService.isAllowed(RbacActivity.EDIT_FIGHT_TIME)) {
      if (editable) {
        this.pauseTimer();
      }
      this.secondsEditable = editable && this.editable;
      this.minutesEditable = false;
    }
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

  dragEnd($event: CdkDragEnd): void {
    this.timerPosition = $event.source.getFreeDragPosition();
  }
}
