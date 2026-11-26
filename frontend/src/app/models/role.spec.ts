import {Role} from './role';
import {RoleType} from './role-type';
import {Tournament} from './tournament';
import {TournamentScore} from './tournament-score.model';
import {Participant} from './participant';

describe('Role', () => {
  const buildTournament = (): Tournament => {
    const tournament = new Tournament();
    tournament.id = 2;
    tournament.name = 'Cup';
    tournament.tournamentScore = new TournamentScore();
    return tournament;
  };

  const buildParticipant = (): Participant => {
    const participant = new Participant();
    participant.id = 3;
    participant.name = 'John';
    participant.lastname = 'Smith';
    participant.idCard = 'ID-3';
    return participant;
  };

  const buildRole = (): Role => {
    const role = new Role();
    role.id = 9;
    role.roleType = RoleType.COMPETITOR;
    role.tournament = buildTournament();
    role.participant = buildParticipant();
    return role;
  };

  it('should copy tournament and participant', () => {
    const source = buildRole();
    const target = new Role();

    Role.copy(source, target);

    expect(target.roleType).toBe(RoleType.COMPETITOR);
    expect(target.tournament.id).toBe(2);
    expect(target.participant.id).toBe(3);
    expect(target.tournament).not.toBe(source.tournament);
    expect(target.participant).not.toBe(source.participant);
  });

  it('should clone role into a new instance', () => {
    const source = buildRole();

    const clone = Role.clone(source);

    expect(clone).not.toBe(source);
    expect(clone.roleType).toBe(RoleType.COMPETITOR);
    expect(clone.participant.lastname).toBe('Smith');
  });

  it('should keep missing tournament and participant undefined when absent', () => {
    const source = new Role();
    source.roleType = RoleType.REFEREE;
    const target = new Role();

    Role.copy(source, target);

    expect(target.roleType).toBe(RoleType.REFEREE);
    expect(target.tournament).toBeUndefined();
    expect(target.participant).toBeUndefined();
  });
});

