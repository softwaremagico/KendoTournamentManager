import {MemberSelectorComponent} from './member-selector.component';
import {Team} from '../../../models/team';
import {Participant} from '../../../models/participant';

describe('MemberSelectorComponent', () => {
  let component: MemberSelectorComponent;

  beforeEach(() => {
    component = new MemberSelectorComponent();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should filter out undefined members from the team when ngOnChanges is called', () => {
    const participant1 = new Participant();
    participant1.name = 'John';
    const participant2 = new Participant();
    participant2.name = 'Jane';

    const team = new Team('Test Team');
    team.members = [participant1, undefined, participant2, undefined];
    component.team = team;

    component.ngOnChanges();

    expect(component.members.length).toBe(2);
    expect(component.members).toEqual([participant1, participant2]);
  });

  it('should handle empty team members list when ngOnChanges is called', () => {
    const team = new Team('Empty Team');
    team.members = [undefined, undefined];
    component.team = team;

    component.ngOnChanges();

    expect(component.members.length).toBe(0);
    expect(component.members).toEqual([]);
  });

  it('should select a single participant when selections is 1', () => {
    spyOn(component.selectedMember, 'emit');
    component.selections = 1;

    const participant = new Participant();
    participant.name = 'Test User';
    component.selectedMembers = [];

    component.selectUser(participant);

    expect(component.selectedMembers.length).toBe(1);
    expect(component.selectedMembers[0]).toEqual(participant);
    expect(component.selectedMember.emit).toHaveBeenCalledOnceWith([participant]);
  });

  it('should replace the previous selection when selections is 1 and a new user is selected', () => {
    spyOn(component.selectedMember, 'emit');
    component.selections = 1;

    const participant1 = new Participant();
    participant1.name = 'User 1';
    const participant2 = new Participant();
    participant2.name = 'User 2';

    component.selectUser(participant1);
    (component.selectedMember.emit as jasmine.Spy).calls.reset();

    component.selectUser(participant2);

    expect(component.selectedMembers.length).toBe(1);
    expect(component.selectedMembers[0]).toEqual(participant2);
    expect(component.selectedMember.emit).toHaveBeenCalledOnceWith([participant2]);
  });

  it('should add participant to selection when selections > 1 and participant is not selected', () => {
    spyOn(component.selectedMember, 'emit');
    component.selections = 2;
    component.selectedMembers = [];

    const participant = new Participant();
    participant.name = 'Test User';

    component.selectUser(participant);

    expect(component.selectedMembers).toContain(participant);
    expect(component.selectedMember.emit).toHaveBeenCalledOnceWith([participant]);
  });

  it('should remove participant from selection when selections > 1 and participant is already selected', () => {
    spyOn(component.selectedMember, 'emit');
    component.selections = 2;

    const participant1 = new Participant();
    participant1.name = 'User 1';
    const participant2 = new Participant();
    participant2.name = 'User 2';

    component.selectedMembers = [participant1, participant2];

    component.selectUser(participant1);

    expect(component.selectedMembers).not.toContain(participant1);
    expect(component.selectedMembers).toEqual([participant2]);
    expect(component.selectedMember.emit).toHaveBeenCalledOnceWith([participant2]);
  });

  it('should allow multiple selections when selections > 1', () => {
    spyOn(component.selectedMember, 'emit');
    component.selections = 3;
    component.selectedMembers = [];

    const participant1 = new Participant();
    participant1.name = 'User 1';
    const participant2 = new Participant();
    participant2.name = 'User 2';
    const participant3 = new Participant();
    participant3.name = 'User 3';

    component.selectUser(participant1);
    component.selectUser(participant2);
    component.selectUser(participant3);

    expect(component.selectedMembers.length).toBe(3);
    expect(component.selectedMembers).toEqual([participant1, participant2, participant3]);
  });
});

