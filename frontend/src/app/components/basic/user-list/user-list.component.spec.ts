import {UserListComponent} from './user-list.component';
import {UserListData} from './user-list-data';
import {FilterResetService} from '../../../services/notifications/filter-reset.service';
import {Participant} from '../../../models/participant';
import {Club} from '../../../models/club';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let userListData: UserListData;
  let filterResetService: FilterResetService;

  beforeEach(() => {
    filterResetService = new FilterResetService();
    component = new UserListComponent(filterResetService);
    userListData = new UserListData();
    component.userListData = userListData;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default input values', () => {
    expect(component.showAvatars).toBeFalse();
  });

  it('should delegate filter calls to userListData', () => {
    spyOn(userListData, 'filter');
    const filterString = 'test filter';

    component.filter(filterString);

    expect(userListData.filter).toHaveBeenCalledOnceWith(filterString);
  });

  it('should reset filter when filterResetService emits a true value', () => {
    spyOn(userListData, 'filter');
    component.ngOnInit();

    filterResetService.resetFilter.next(true);

    expect(userListData.filter).toHaveBeenCalledWith('');
  });

  it('should not reset filter when filterResetService emits a false value', () => {
    spyOn(userListData, 'filter');
    component.ngOnInit();
    (userListData.filter as jasmine.Spy).calls.reset();

    filterResetService.resetFilter.next(false);

    expect(userListData.filter).not.toHaveBeenCalled();
  });

  it('should filter participants by name', () => {
    const participant1 = new Participant();
    participant1.name = 'John';
    participant1.lastname = 'Doe';

    const participant2 = new Participant();
    participant2.name = 'Jane';
    participant2.lastname = 'Smith';

    userListData.participants = [participant1, participant2];
    component.filter('john');

    expect(userListData.filteredParticipants.length).toBe(1);
    expect(userListData.filteredParticipants[0]).toEqual(participant1);
  });

  it('should filter participants by lastname', () => {
    const participant1 = new Participant();
    participant1.name = 'John';
    participant1.lastname = 'Doe';

    const participant2 = new Participant();
    participant2.name = 'Jane';
    participant2.lastname = 'Smith';

    userListData.participants = [participant1, participant2];
    component.filter('smith');

    expect(userListData.filteredParticipants.length).toBe(1);
    expect(userListData.filteredParticipants[0]).toEqual(participant2);
  });

  it('should filter participants by ID card', () => {
    const participant1 = new Participant();
    participant1.name = 'John';
    participant1.idCard = '12345678A';

    const participant2 = new Participant();
    participant2.name = 'Jane';
    participant2.idCard = '87654321B';

    userListData.participants = [participant1, participant2];
    component.filter('12345678a');

    expect(userListData.filteredParticipants.length).toBe(1);
    expect(userListData.filteredParticipants[0]).toEqual(participant1);
  });

  it('should filter participants by club name', () => {
    const club1 = new Club();
    club1.name = 'Kendo Club';

    const participant1 = new Participant();
    participant1.name = 'John';
    participant1.club = club1;

    const participant2 = new Participant();
    participant2.name = 'Jane';
    participant2.club = undefined;

    userListData.participants = [participant1, participant2];
    component.filter('kendo');

    expect(userListData.filteredParticipants.length).toBe(1);
    expect(userListData.filteredParticipants[0]).toEqual(participant1);
  });

  it('should handle empty participants list when filtering', () => {
    userListData.participants = [];
    component.filter('any');

    expect(userListData.filteredParticipants).toEqual([]);
  });

  it('should show all participants when filter is empty', () => {
    const participant1 = new Participant();
    participant1.name = 'John';

    const participant2 = new Participant();
    participant2.name = 'Jane';

    userListData.participants = [participant1, participant2];
    component.filter('');

    expect(userListData.filteredParticipants.length).toBe(2);
    expect(userListData.filteredParticipants).toEqual([participant1, participant2]);
  });
});

