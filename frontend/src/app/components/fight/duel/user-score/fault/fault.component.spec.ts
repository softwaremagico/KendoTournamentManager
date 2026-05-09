import {SimpleChange} from '@angular/core';
import {Duel} from '../../../../../models/duel';
import {DuelService} from '../../../../../services/duel.service';
import {MessageService} from '../../../../../services/message.service';
import {ScoreUpdatedService} from '../../../../../services/notifications/score-updated.service';
import {RbacService} from '../../../../../services/rbac/rbac.service';
import {of} from 'rxjs';
import {FaultComponent} from './fault.component';

describe('FaultComponent', () => {
  let component: FaultComponent;
  let duelServiceSpy: jasmine.SpyObj<DuelService>;
  let scoreUpdatedServiceMock: jasmine.SpyObj<ScoreUpdatedService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let translateServiceSpy: jasmine.SpyObj<any>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  const buildDuel = (): Duel => {
    const d = new Duel();
    d.competitor1Score = [];
    d.competitor2Score = [];
    d.competitor1ScoreTime = [];
    d.competitor2ScoreTime = [];
    d.competitor1Fault = false;
    d.competitor2Fault = false;
    d.competitor1FaultTime = undefined;
    d.competitor2FaultTime = undefined;
    d.duration = 120;
    return d;
  };

  beforeEach(() => {
    duelServiceSpy = jasmine.createSpyObj('DuelService', ['update']);
    scoreUpdatedServiceMock = jasmine.createSpyObj('ScoreUpdatedService', [], {
      isScoreUpdated: { next: jasmine.createSpy('next') }
    });
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'warningMessage']);
    translateServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    component = new FaultComponent(
      duelServiceSpy,
      scoreUpdatedServiceMock as any,
      messageServiceSpy,
      translateServiceSpy,
      rbacServiceSpy
    );
    duelServiceSpy.update.and.returnValue(of(buildDuel()));
    component.duel = buildDuel();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should run ngOnInit without throwing', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should call setTime when duel changes in ngOnChanges', () => {
    spyOn(component, 'setTime');
    component.ngOnChanges({ duel: new SimpleChange(null, component.duel, true) });
    expect(component.setTime).toHaveBeenCalled();
  });

  it('should call setTime when left changes in ngOnChanges', () => {
    spyOn(component, 'setTime');
    component.ngOnChanges({ left: new SimpleChange(false, true, false) });
    expect(component.setTime).toHaveBeenCalled();
  });

  it('should NOT call setTime when an unrelated input changes', () => {
    spyOn(component, 'setTime');
    component.ngOnChanges({ locked: new SimpleChange(false, true, false) });
    expect(component.setTime).not.toHaveBeenCalled();
  });

  it('should set competitor1Fault and duration time when left=true, swapTeams=false, fault=true', () => {
    component.left = true;
    component.swapTeams = false;

    component.updateFault(true);

    expect(component.duel.competitor1Fault).toBeTrue();
    expect(component.duel.competitor1FaultTime).toBe(120);
  });

  it('should clear competitor1FaultTime when fault is set to false', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1Fault = true;
    component.duel.competitor1FaultTime = 120;

    component.updateFault(false);

    expect(component.duel.competitor1Fault).toBeFalse();
    expect(component.duel.competitor1FaultTime).toBeUndefined();
  });

  it('should set competitor2Fault when left=false and swapTeams=false', () => {
    component.left = false;
    component.swapTeams = false;

    component.updateFault(true);

    expect(component.duel.competitor2Fault).toBeTrue();
    expect(component.duel.competitor2FaultTime).toBe(120);
  });

  it('should set timeRepresentation in seconds only when fault time < 60', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1FaultTime = 45;
    translateServiceSpy.translate.and.returnValue('s');

    component.setTime();

    expect(component.timeRepresentation).toContain('45');
  });

  it('should set timeRepresentation with minutes and seconds when fault time >= 60', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1FaultTime = 90;
    translateServiceSpy.translate.and.returnValue('x');

    component.setTime();

    expect(component.timeRepresentation).toContain('1');
    expect(component.timeRepresentation).toContain('30');
  });

  it('should set timeRepresentation to undefined when no fault time', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1FaultTime = undefined;

    component.setTime();

    expect(component.timeRepresentation).toBeUndefined();
  });

  it('should return empty string from tooltipText when timeRepresentation is empty', () => {
    component.timeRepresentation = '';
    expect(component.tooltipText()).toBe('');
  });

  it('should return html from tooltipText when timeRepresentation is set', () => {
    component.timeRepresentation = '2 min';
    translateServiceSpy.translate.and.returnValue('fault');
    expect(component.tooltipText()).toContain('2 min');
  });

  it('should update mouseX and mouseY on updateCoordinates', () => {
    const event = { clientX: 300, clientY: 200 } as MouseEvent;
    component.updateCoordinates(event);
    expect(component.mouseX).toBe(300);
    expect(component.mouseY).toBe(200);
  });

  it('should clear mouseX and mouseY on clearCoordinates', () => {
    component.mouseX = 100;
    component.mouseY = 50;
    component.clearCoordinates({} as MouseEvent);
    expect(component.mouseX).toBeUndefined();
    expect(component.mouseY).toBeUndefined();
  });
});

