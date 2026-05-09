import {BehaviorSubject} from 'rxjs';
import {Duel} from '../../models/duel';
import {DuelChangedService} from '../../services/notifications/duel-changed.service';
import {UntieFightComponent} from './untie-fight.component';

describe('UntieFightComponent', () => {
  let component: UntieFightComponent;
  let duelChangedSubject: BehaviorSubject<Duel>;
  let duelChangedService: DuelChangedService;

  const createDuel = (finished: boolean): Duel => {
    const duel = new Duel();
    duel.finished = finished;
    duel.competitor1Score = [];
    duel.competitor2Score = [];
    duel.competitor1ScoreTime = [];
    duel.competitor2ScoreTime = [];
    return duel;
  };

  beforeEach(() => {
    duelChangedSubject = new BehaviorSubject<Duel>(createDuel(false));
    duelChangedService = {
      isDuelUpdated: duelChangedSubject
    } as DuelChangedService;

    component = new UntieFightComponent(duelChangedService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should update duelSelected when DuelChangedService emits a new duel', () => {
    const updatedDuel = createDuel(true);

    component.ngOnInit();
    duelChangedSubject.next(updatedDuel);

    expect(component.duelSelected).toBe(updatedDuel);
  });

  it('should emit selected duel wrapped in an array when selectDuel is called', () => {
    const duel = createDuel(false);
    spyOn(component.selectedDuel, 'emit');

    component.selectDuel(duel);

    expect(component.selectedDuel.emit).toHaveBeenCalledOnceWith([duel]);
  });

  it('should return true from isOver when duel.finished is true', () => {
    const duel = createDuel(true);

    expect(component.isOver(duel)).toBeTrue();
  });

  it('should return false from isOver when duel.finished is false', () => {
    const duel = createDuel(false);

    expect(component.isOver(duel)).toBeFalse();
  });

  it('should stop updating duelSelected after ngOnDestroy', () => {
    const initialDuel = createDuel(false);
    const duelAfterDestroy = createDuel(true);

    component.ngOnInit();
    duelChangedSubject.next(initialDuel);
    component.ngOnDestroy();
    duelChangedSubject.next(duelAfterDestroy);

    expect(component.duelSelected).toBe(initialDuel);
  });
});

