import { Duel } from '../../../../models/duel';
import { DuelType } from '../../../../models/duel-type';
import { Participant } from '../../../../models/participant';
import { UserScoreComponent } from './user-score.component';

describe('UserScoreComponent', () => {
  let component: UserScoreComponent;

  const createParticipant = (id: number, name: string): Participant => ({
    id,
    name,
    lastname: 'Test',
    idCard: `ID${id}`,
    hasAvatar: false,
    locked: false
  } as unknown as Participant);

  beforeEach(() => {
    component = new UserScoreComponent();
    component.duel = new Duel();
    component.duel.competitor1 = createParticipant(1, 'A');
    component.duel.competitor2 = createParticipant(2, 'B');
    component.duel.competitor1Score = [];
    component.duel.competitor2Score = [];
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should return true in isUntie when duel type is UNDRAW', () => {
    component.duel.type = DuelType.UNDRAW;

    expect(component.isUntie()).toBeTrue();
  });

  it('should return false in isUntie when duel type is STANDARD', () => {
    component.duel.type = DuelType.STANDARD;

    expect(component.isUntie()).toBeFalse();
  });

  it('should return competitor1 when left is true and swapTeams is false', () => {
    component.left = true;
    component.swapTeams = false;

    expect(component.getParticipant()).toBe(component.duel.competitor1);
  });

  it('should return competitor2 when left is true and swapTeams is true', () => {
    component.left = true;
    component.swapTeams = true;

    expect(component.getParticipant()).toBe(component.duel.competitor2);
  });

  it('should return competitor2 when left is false and swapTeams is false', () => {
    component.left = false;
    component.swapTeams = false;

    expect(component.getParticipant()).toBe(component.duel.competitor2);
  });

  it('should return competitor1 when left is false and swapTeams is true', () => {
    component.left = false;
    component.swapTeams = true;

    expect(component.getParticipant()).toBe(component.duel.competitor1);
  });

  it('should update participantWindowOpened when photo visibility event changes', () => {
    component.isParticipantPhotoWindowVisible(true);
    expect(component.participantWindowOpened).toBeTrue();

    component.isParticipantPhotoWindowVisible(false);
    expect(component.participantWindowOpened).toBeFalse();
  });
});

