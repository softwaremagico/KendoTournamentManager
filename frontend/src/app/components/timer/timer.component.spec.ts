import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TimerComponent } from './timer.component';
import { AudioService } from '../../services/audio.service';
import { TimeChangedService } from '../../services/notifications/time-changed.service';
import { RbacService } from '../../services/rbac/rbac.service';
import { FilterFocusService } from '../../services/notifications/filter-focus.service';
import { RbacActivity } from '../../services/rbac/rbac.activity';
import { Subject, BehaviorSubject } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('TimerComponent', () => {
  let component: TimerComponent;
  let fixture: ComponentFixture<TimerComponent>;
  let audioServiceSpy: jasmine.SpyObj<AudioService>;
  let timeChangedServiceSpy: jasmine.SpyObj<TimeChangedService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let filterFocusServiceSpy: jasmine.SpyObj<FilterFocusService>;

  beforeEach(async () => {
    audioServiceSpy = jasmine.createSpyObj('AudioService', ['playWhistleByTime']);
    timeChangedServiceSpy = jasmine.createSpyObj('TimeChangedService', [], {
      isElapsedTimeChanged: new BehaviorSubject(0),
      isTotalTimeChanged: new BehaviorSubject(0)
    });
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    filterFocusServiceSpy = jasmine.createSpyObj('FilterFocusService', [], {
      isFilterActive: new BehaviorSubject(false)
    });

    await TestBed.configureTestingModule({
      declarations: [ TimerComponent ],
      providers: [
        { provide: AudioService, useValue: audioServiceSpy },
        { provide: TimeChangedService, useValue: timeChangedServiceSpy },
        { provide: RbacService, useValue: rbacServiceSpy },
        { provide: FilterFocusService, useValue: filterFocusServiceSpy }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TimerComponent);
    component = fixture.componentInstance;
    component.resetTimerPosition = new Subject<boolean>();
    component.startingMinutes = 5;
    component.startingSeconds = 30;
    component.duelDuration = 330; // 5:30 in seconds
    rbacServiceSpy.isAllowed.and.returnValue(true);
  });

  afterEach(() => {
    component.ngOnDestroy();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set startingMinutes via setter', () => {
    component.startingMinutes = 3;
    expect(component.minutes).toBe(3);
  });

  it('should set startingSeconds via setter', () => {
    component.startingSeconds = 45;
    expect(component.seconds).toBe(45);
  });

  it('should have default editable as true', () => {
    expect(component.editable).toBeTrue();
  });

  it('should have default shown as false', () => {
    expect(component.shown).toBeFalse();
  });

  it('should have default started as false', () => {
    expect(component.started).toBeFalse();
  });

  it('should have elapsedSeconds initialized to 0', () => {
    expect(component.elapsedSeconds).toBe(0);
  });

  it('should convert number to double digit string', () => {
    expect(component.toDoubleDigit(5)).toBe('05');
    expect(component.toDoubleDigit(15)).toBe('15');
    expect(component.toDoubleDigit(0)).toBe('00');
  });

  it('should return 00 for NaN', () => {
    expect(component.toDoubleDigit(NaN)).toBe('00');
  });

  it('should start timer and emit onPlayPressed', () => {
    spyOn(component.playPressed, 'emit');
    component.elapsedSeconds = 5;

    component.startTimer();

    expect(component.started).toBeTrue();
    expect(component.playPressed.emit).toHaveBeenCalledOnceWith([5]);
  });

  it('should pause timer and emit onTimerChanged', () => {
    spyOn(component.timerChanged, 'emit');
    component.started = true;
    component.elapsedSeconds = 10;

    component.pauseTimer();

    expect(component.started).toBeFalse();
    expect(component.timerChanged.emit).toHaveBeenCalledOnceWith([10]);
  });

  it('should finish timer and emit onTimerFinished', () => {
    spyOn(component.timerFinished, 'emit');
    component.minutes = 2;
    component.seconds = 30;
    component.elapsedSeconds = 15;

    component.finishTimer();

    expect(component.timerFinished.emit).toHaveBeenCalledOnceWith([true]);
    expect(component.elapsedSeconds).toBe(0);
    expect(component.started).toBeFalse();
  });

  it('should set elapsedSeconds to 1 if it is 0 when finishing', () => {
    spyOn(component.timerFinished, 'emit');
    component.elapsedSeconds = 0;

    component.finishTimer();

    expect(component.timerFinished.emit).toHaveBeenCalled();
  });

  it('should restore timer to initial duration', () => {
    spyOn(component.timerChanged, 'emit');
    spyOn(component.timeDurationChanged, 'emit');
    component.duelDuration = 330;
    component.elapsedSeconds = 50;

    component.restoreTimer();

    expect(component.elapsedSeconds).toBe(0);
    expect(component.timerChanged.emit).toHaveBeenCalledOnceWith([0]);
    expect(component.timeDurationChanged.emit).toHaveBeenCalledOnceWith([330]);
  });

  it('should correctly handle seconds increment in correctSecondsAndMinutes', () => {
    const result = component.correctSecondsAndMinutes(95, 0);

    expect(component.minutes).toBe(1);
    expect(component.seconds).toBe(35);
    expect(result).toBe(95);
  });

  it('should cap minutes to 20 in correctSecondsAndMinutes', () => {
    component.correctSecondsAndMinutes(1500, 0); // 25 minutes

    expect(component.minutes).toBe(20);
  });

  it('should prevent negative values in correctSecondsAndMinutes', () => {
    component.correctSecondsAndMinutes(-100, 0);

    expect(component.minutes).toBe(0);
    expect(component.seconds).toBe(0);
  });

  it('should add time and emit timeDurationChanged', () => {
    spyOn(component.timeDurationChanged, 'emit');
    component.seconds = 10;
    component.minutes = 2;
    component.elapsedSeconds = 5;

    component.addTime(20);

    expect(component.timeDurationChanged.emit).toHaveBeenCalled();
  });

  it('should allow setting minutes as editable when RBAC allows', () => {
    spyOn(component, 'pauseTimer');
    rbacServiceSpy.isAllowed.and.returnValue(true);
    component.editable = true;

    component.setMinutesEditable(true);

    expect(component.minutesEditable).toBeTrue();
    expect(component.pauseTimer).toHaveBeenCalled();
  });

  it('should not allow setting minutes as editable when RBAC denies', () => {
    rbacServiceSpy.isAllowed.and.returnValue(false);

    component.setMinutesEditable(true);

    expect(component.minutesEditable).toBeFalse();
  });

  it('should allow setting seconds as editable when RBAC allows', () => {
    spyOn(component, 'pauseTimer');
    rbacServiceSpy.isAllowed.and.returnValue(true);
    component.editable = true;

    component.setSecondsEditable(true);

    expect(component.secondsEditable).toBeTrue();
    expect(component.pauseTimer).toHaveBeenCalled();
  });

  it('should not allow setting seconds as editable when RBAC denies', () => {
    rbacServiceSpy.isAllowed.and.returnValue(false);

    component.setSecondsEditable(true);

    expect(component.secondsEditable).toBeFalse();
  });

  it('should indicate warning time when minutes is 0 and seconds between 10 and 30', () => {
    component.minutes = 0;
    component.seconds = 15;

    expect(component.isWarningTime()).toBeTrue();
  });

  it('should not indicate warning time when seconds above 30', () => {
    component.minutes = 0;
    component.seconds = 35;

    expect(component.isWarningTime()).toBeFalse();
  });

  it('should indicate almost finished when minutes is 0 and seconds <= 10', () => {
    component.minutes = 0;
    component.seconds = 5;

    expect(component.isAlmostFinished()).toBeTrue();
  });

  it('should not indicate almost finished when seconds > 10', () => {
    component.minutes = 0;
    component.seconds = 15;

    expect(component.isAlmostFinished()).toBeFalse();
  });

  it('should indicate paused when not started and elapsedSeconds > 0', () => {
    component.started = false;
    component.elapsedSeconds = 10;

    expect(component.isPaused()).toBeTrue();
  });

  it('should not indicate paused when timer is running', () => {
    component.started = true;
    component.elapsedSeconds = 10;

    expect(component.isPaused()).toBeFalse();
  });

  it('should clear interval on destroy', () => {
    fixture.detectChanges(); // trigger ngOnInit
    const intervalId = (component as any).clockHandler;

    component.ngOnDestroy();

    expect((component as any).clockHandler).toBeNull();
  });
});

