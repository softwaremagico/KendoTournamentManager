import {TeamListComponent} from './team-list.component';
import {TeamListData} from './team-list-data';
import {Team} from '../../../models/team';
import {Participant} from '../../../models/participant';

describe('TeamListComponent', () => {
  let component: TeamListComponent;
  let teamListData: TeamListData;

  beforeEach(() => {
    component = new TeamListComponent();
    teamListData = new TeamListData();
    component.teamListData = teamListData;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default input values', () => {
    expect(component.minify).toBeFalse();
    expect(component.horizontal).toBeFalse();
    expect(component.grid).toBeFalse();
    expect(component.teamsDragDisabled).toEqual([]);
  });

  it('should delegate filter call to teamListData', () => {
    spyOn(teamListData, 'filter');
    const filterString = 'test filter';

    component.filter(filterString);

    expect(teamListData.filter).toHaveBeenCalledOnceWith(filterString);
  });

  it('should reset filter by calling filter with empty string', () => {
    spyOn(teamListData, 'filter');

    component.reset();

    expect(teamListData.filter).toHaveBeenCalledOnceWith('');
  });

  it('should return true when team is in the dragDisabled list', () => {
    const team1 = new Team('Team 1');
    const team2 = new Team('Team 2');
    component.teamsDragDisabled = [team1, team2];

    expect(component.isDragDisabled(team1)).toBeTrue();
    expect(component.isDragDisabled(team2)).toBeTrue();
  });

  it('should return false when team is not in the dragDisabled list', () => {
    const team1 = new Team('Team 1');
    const team2 = new Team('Team 2');
    const team3 = new Team('Team 3');
    component.teamsDragDisabled = [team1, team2];

    expect(component.isDragDisabled(team3)).toBeFalse();
  });

  it('should return false when dragDisabled list is empty', () => {
    const team = new Team('Test Team');
    component.teamsDragDisabled = [];

    expect(component.isDragDisabled(team)).toBeFalse();
  });

  it('should handle filtering with empty teams list', () => {
    teamListData.teams = [];
    component.filter('any');

    expect(teamListData.filteredTeams).toEqual([]);
  });

  it('should filter teams by name', () => {
    const team1 = new Team('Kendo Team');
    team1.members = [];
    const team2 = new Team('Judo Team');
    team2.members = [];
    const team3 = new Team('Karate Team');
    team3.members = [];

    teamListData.teams = [team1, team2, team3];
    component.filter('kendo');

    expect(teamListData.filteredTeams.length).toBe(1);
    expect(teamListData.filteredTeams[0]).toEqual(team1);
  });

  it('should filter teams by member name (case-insensitive)', () => {
    const participant1 = new Participant();
    participant1.name = 'John';
    participant1.lastname = 'Doe';

    const participant2 = new Participant();
    participant2.name = 'Jane';
    participant2.lastname = 'Smith';

    const team1 = new Team('Team A');
    team1.members = [participant1];

    const team2 = new Team('Team B');
    team2.members = [participant2];

    teamListData.teams = [team1, team2];
    component.filter('john');

    expect(teamListData.filteredTeams.length).toBe(1);
    expect(teamListData.filteredTeams[0]).toEqual(team1);
  });
});



