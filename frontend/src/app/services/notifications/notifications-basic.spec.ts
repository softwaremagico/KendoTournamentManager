import {UserSessionService} from '../user-session.service';
import {DarkModeService} from './dark-mode.service';
import {TimeChangedService} from './time-changed.service';
import {DuelChangedService} from './duel-changed.service';
import {UntieAddedService} from './untie-added.service';
import {GroupUpdatedService} from './group-updated.service';
import {ProjectModeChangedService} from './project-mode-changed.service';
import {StatisticsChangedService} from './statistics-changed.service';
import {NumberOfWinnersUpdatedService} from './number-of-winners-updated.service';
import {PictureUpdatedService} from './picture-updated.service';
import {Duel} from '../../models/duel';
import {Group} from '../../models/group';
import {Fight} from '../../models/fight';
import {MembersOrderChangedService} from './members-order-changed.service';

describe('Notification services', () => {
  it('should initialize DarkModeService from user session preference', () => {
    const userSessionServiceSpy = jasmine.createSpyObj<UserSessionService>('UserSessionService', ['getNightMode']);
    userSessionServiceSpy.getNightMode.and.returnValue(true);

    const service = new DarkModeService(userSessionServiceSpy);

    expect(service.darkModeSwitched.value).toBeTrue();
  });

  it('should expose default values for time changed service', () => {
    const service = new TimeChangedService();

    expect(service.isElapsedTimeChanged.value).toBe(0);
    expect(service.isTotalTimeChanged.value).toBe(0);
  });

  it('should expose default duel changed value', () => {
    const service = new DuelChangedService();

    expect(service.isDuelUpdated.value).toEqual(jasmine.any(Duel));
  });

  it('should expose default untie added value', () => {
    const service = new UntieAddedService();

    expect(service.isDuelsAdded.value).toEqual([]);
  });

  it('should expose default group updated value', () => {
    const service = new GroupUpdatedService();

    expect(service.isGroupUpdated.value).toEqual(jasmine.any(Group));
  });

  it('should expose default members order values', () => {
    const service = new MembersOrderChangedService();

    expect(service.membersOrderChanged.value).toEqual(jasmine.any(Fight));
    expect(service.membersOrderAllowed.value).toBeFalse();
  });

  it('should expose default project mode value', () => {
    const service = new ProjectModeChangedService();

    expect(service.isProjectMode.value).toBeFalse();
  });

  it('should expose default statistics changed value', () => {
    const service = new StatisticsChangedService();

    expect(service.areStatisticsChanged.value).toBeFalse();
  });

  it('should expose default number of winners value', () => {
    const service = new NumberOfWinnersUpdatedService();

    expect(service.numberOfWinners.value).toBe(1);
  });

  it('should expose default picture updated value', () => {
    const service = new PictureUpdatedService();

    expect(service.isPictureUpdated.value).toBe('');
  });
});
