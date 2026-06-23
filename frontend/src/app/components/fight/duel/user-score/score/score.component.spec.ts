import {SimpleChange} from '@angular/core';
import {BehaviorSubject, of} from 'rxjs';
import {Duel} from '../../../../../models/duel';
import {Score} from '../../../../../models/score';
import {DuelService} from '../../../../../services/duel.service';
import {MessageService} from '../../../../../services/message.service';
import {ScoreUpdatedService} from '../../../../../services/notifications/score-updated.service';
import {RbacService} from '../../../../../services/rbac/rbac.service';
import {ScoreComponent} from './score.component';

describe('ScoreComponent', () => {
  let component: ScoreComponent;
  let duelServiceSpy: jasmine.SpyObj<DuelService>;
  let scoreUpdatedSubject: BehaviorSubject<Duel>;
  let scoreUpdatedService: Partial<ScoreUpdatedService>;
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
    d.duration = 180;
    const p1 = { id: 1, name: 'A' } as any;
    const p2 = { id: 2, name: 'B' } as any;
    d.competitor1 = p1;
    d.competitor2 = p2;
    return d;
  };

  beforeEach(() => {
    scoreUpdatedSubject = new BehaviorSubject<Duel>(buildDuel());
    scoreUpdatedService = { isScoreUpdated: scoreUpdatedSubject };
    duelServiceSpy = jasmine.createSpyObj('DuelService', ['update']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);
    translateServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    duelServiceSpy.update.and.returnValue(of(buildDuel()));

    component = new ScoreComponent(
      duelServiceSpy,
      scoreUpdatedService as ScoreUpdatedService,
      messageServiceSpy,
      translateServiceSpy,
      rbacServiceSpy
    );
    component.duel = buildDuel();
    component.index = 0;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should update scoreRepresentation when subscribed duel matches on ngOnInit', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1Score[0] = Score.MEN;
    component.ngOnInit();
    scoreUpdatedSubject.next(component.duel);
    expect(component.scoreRepresentation).toBe('M');
  });

  it('should call setTime on ngOnChanges for duel input change', () => {
    spyOn(component, 'setTime');
    component.ngOnChanges({ duel: new SimpleChange(null, component.duel, true) });
    expect(component.setTime).toHaveBeenCalled();
  });

  it('should get competitor1 score when left=true swapTeams=false', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1Score[0] = Score.KOTE;
    expect(component.getScore()).toBe(Score.KOTE);
  });

  it('should get competitor2 score when left=true swapTeams=true', () => {
    component.left = true;
    component.swapTeams = true;
    component.duel.competitor2Score[0] = Score.DO;
    expect(component.getScore()).toBe(Score.DO);
  });

  it('should get competitor2 score when left=false swapTeams=false', () => {
    component.left = false;
    component.swapTeams = false;
    component.duel.competitor2Score[0] = Score.TSUKI;
    expect(component.getScore()).toBe(Score.TSUKI);
  });

  it('should get competitor1 score when left=false swapTeams=true', () => {
    component.left = false;
    component.swapTeams = true;
    component.duel.competitor1Score[0] = Score.MEN;
    expect(component.getScore()).toBe(Score.MEN);
  });

  it('should return correct tag from getScoreRepresentation', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1Score[0] = Score.MEN;
    expect(component.getScoreRepresentation()).toBe('M');
  });

  it('should update competitor1Score and call duelService on updateScore', () => {
    component.left = true;
    component.swapTeams = false;
    component.updateScore(Score.KOTE);
    expect(component.duel.competitor1Score[0]).toBe(Score.KOTE);
    expect(duelServiceSpy.update).toHaveBeenCalled();
  });

  it('should return empty array from possibleScores when locked', () => {
    component.locked = true;
    expect(component.possibleScores()).toEqual([]);
  });

  it('should return full score array from possibleScores when unlocked and both competitors exist', () => {
    component.locked = false;
    component.left = true;
    component.swapTeams = false;
    const scores = component.possibleScores();
    expect(scores.length).toBeGreaterThan(0);
  });

  it('should set timeRepresentation to undefined when score time is falsy', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1ScoreTime[0] = undefined as any;
    component.setTime();
    expect(component.timeRepresentation).toBeUndefined();
  });

  it('should set timeRepresentation with seconds only when score time < 60', () => {
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1ScoreTime[0] = 45;
    translateServiceSpy.translate.and.returnValue('s');
    component.setTime();
    expect(component.timeRepresentation).toContain('45');
  });

  it('should return empty from tooltipText when timeRepresentation is falsy', () => {
    component.timeRepresentation = undefined;
    expect(component.tooltipText()).toBe('');
  });

  it('should return html from tooltipText when timeRepresentation is set', () => {
    component.timeRepresentation = '1 min';
    component.left = true;
    component.swapTeams = false;
    component.duel.competitor1Score[0] = Score.MEN;
    const result = component.tooltipText();
    expect(result).toContain('1 min');
  });

  it('should update mouseX and mouseY on updateCoordinates', () => {
    component.updateCoordinates({ clientX: 200, clientY: 100 } as MouseEvent);
    expect(component.mouseX).toBe(200);
    expect(component.mouseY).toBe(100);
  });

  it('should clear mouseX and mouseY on clearCoordinates', () => {
    component.mouseX = 10;
    component.mouseY = 20;
    component.clearCoordinates();
    expect(component.mouseX).toBeUndefined();
    expect(component.mouseY).toBeUndefined();
  });
});

