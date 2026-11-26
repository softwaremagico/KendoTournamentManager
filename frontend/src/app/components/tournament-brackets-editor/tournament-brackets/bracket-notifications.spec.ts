import {Group} from '../../../models/group';
import {Team} from '../../../models/team';
import {Tournament} from '../../../models/tournament';
import {GroupsUpdatedService} from './groups-updated.service';
import {TournamentChangedService} from './tournament-changed.service';

describe('Tournament bracket notifications', () => {
  it('publishes group, team, relation and total-team updates', () => {
    const service = new GroupsUpdatedService();
    const groups = [new Group()];
    const teams = [new Team('Red')];
    const relations = new Map<number, {src: number, dest: number, winner: number}[]>();

    service.areGroupsUpdated.next(groups);
    service.areTeamListUpdated.next(teams);
    service.areRelationsUpdated.next(relations);
    service.areTotalTeamsNumberUpdated.next(5);

    expect(service.areGroupsUpdated.value).toBe(groups);
    expect(service.areTeamListUpdated.value).toBe(teams);
    expect(service.areRelationsUpdated.value).toBe(relations);
    expect(service.areTotalTeamsNumberUpdated.value).toBe(5);
  });

  it('publishes the changed tournament', () => {
    const service = new TournamentChangedService();
    const tournament = new Tournament();
    tournament.name = 'League';

    service.isTournamentChanged.next(tournament);

    expect(service.isTournamentChanged.value).toBe(tournament);
  });
});
