import {Duel} from './duel';
import {Fight} from './fight';
import {Group} from './group';
import {Participant} from './participant';
import {Score} from './score';
import {Team} from './team';
import {Tournament} from './tournament';
import {TournamentType} from './tournament-type';

describe('Tournament bracket domain models', () => {
  let tournament: Tournament;

  beforeEach(() => {
    tournament = new Tournament();
    tournament.id = 1;
    tournament.name = 'League';
    tournament.type = TournamentType.LEAGUE;
    tournament.shiaijos = 2;
    tournament.duelsDuration = 180;
    tournament.locked = false;
  });

  it('clones duels, including scores, competitors and their tournament', () => {
    const source = new Duel();
    source.id = 3;
    source.competitor1 = new Participant();
    source.competitor1.name = 'Aki';
    source.competitor2 = new Participant();
    source.competitor2.name = 'Bea';
    source.competitor1Fault = true;
    source.competitor2Fault = false;
    source.duration = 180;
    source.totalDuration = 200;
    source.competitor1Score = [Score.MEN];
    source.competitor2Score = [Score.KOTE];
    source.competitor1ScoreTime = [10];
    source.competitor2ScoreTime = [20];
    source.competitor1FaultTime = 30;
    source.competitor2FaultTime = undefined;
    source.finished = true;
    source.startedAt = new Date('2026-01-01T10:00:00Z');
    source.finishedAt = new Date('2026-01-01T10:03:20Z');
    source.tournament = tournament;
    source.substitute = true;

    const copy = new Duel();
    copy.competitor1ScoreTime = [];
    copy.competitor2ScoreTime = [];
    Duel.copy(source, copy);

    expect(copy.id).toBe(3);
    expect(copy.competitor1?.name).toBe('Aki');
    expect(copy.competitor1).not.toBe(source.competitor1);
    expect(copy.competitor2?.name).toBe('Bea');
    expect(copy.competitor1Score).toEqual([Score.MEN]);
    expect(copy.competitor1ScoreTime).toEqual([10]);
    expect(copy.competitor2Score).toEqual([Score.KOTE]);
    expect(copy.competitor2ScoreTime).toEqual([20]);
    expect(copy.duration).toBe(180);
    expect(copy.totalDuration).toBe(200);
    expect(copy.finished).toBeTrue();
    expect(copy.startedAt).toEqual(source.startedAt);
    expect(copy.finishedAt).toEqual(source.finishedAt);
    expect(copy.tournament.name).toBe('League');
    expect(copy.substitute).toBeTrue();
  });

  it('adds hansoku only while a slot is free and reports duel activity', () => {
    const duel = new Duel();
    duel.duration = 180;
    duel.competitor1Score = [];
    duel.competitor2Score = [];
    duel.competitor1ScoreTime = [];
    duel.competitor2ScoreTime = [];

    expect(Duel.addHansoku(duel, true)).toBeTrue();
    expect(Duel.addHansoku(duel, false)).toBeTrue();
    expect(duel.competitor1Score[1]).toBe(Score.HANSOKU);
    expect(duel.competitor1ScoreTime[1]).toBe(180);
    expect(duel.competitor2Score[1]).toBe(Score.HANSOKU);
    expect(duel.isStarted()).toBeTrue();
  });

  it('clones fights with their tournament and teams', () => {
    const source = new Fight();
    source.id = 4;
    source.tournament = tournament;
    source.team1 = new Team('Red');
    source.team1.tournament = tournament;
    source.team2 = new Team('White');
    source.team2.tournament = tournament;
    source.shiaijo = 1;
    source.level = 2;
    source.finishedAt = new Date('2026-01-01T10:05:00Z');
    source.duels = [];

    const copy = Fight.clone(source);

    expect(copy.id).toBe(4);
    expect(copy.tournament.name).toBe('League');
    expect(copy.team1.name).toBe('Red');
    expect(copy.team2.name).toBe('White');
    expect(copy.shiaijo).toBe(1);
    expect(copy.level).toBe(2);
    expect(copy.finishedAt).toEqual(source.finishedAt);
    expect(copy.duels).toEqual([]);
  });

  it('clones groups and determines whether they are finished', () => {
    const source = new Group();
    source.id = 5;
    source.tournament = tournament;
    source.teams = [new Team('Red')];
    source.shiaijo = 1;
    source.level = 0;
    source.index = 0;
    source.numberOfWinners = 1;
    source.fights = [];
    source.unties = [];

    const copy = Group.clone(source);

    expect(copy.id).toBe(5);
    expect(copy.tournament.name).toBe('League');
    expect(copy.shiaijo).toBe(1);
    expect(copy.level).toBe(0);
    expect(copy.index).toBe(0);
    expect(copy.numberOfWinners).toBe(1);
    expect(copy.teams).toEqual([]);
    expect(copy.fights).toEqual([]);
    expect(copy.isFinished()).toBeTrue();
    expect(Group.isFinished(source)).toBeTrue();
    expect(copy.toString()).toContain('Group{');
  });

  it('clones teams and preserves their editing and locking flags', () => {
    const source = new Team('Kendo');
    source.id = 6;
    source.name = 'Kendo';
    source.members = [new Participant(), undefined];
    source.tournament = tournament;
    source.editing = true;
    source.locked = false;

    const copy = Team.clone(source);

    expect(copy.id).toBe(6);
    expect(copy.name).toBe('Kendo');
    expect(copy.members).toEqual([]);
    expect(copy.tournament.name).toBe('League');
    expect(copy.editing).toBeFalse();
    expect(copy.locked).toBeTrue();
  });

  it('clones tournaments and resolves the score-rule hint', () => {
    const copy = Tournament.clone(tournament);

    expect(copy.name).toBe('League');
    expect(copy.shiaijos).toBe(2);
    expect(copy.teamSize).toBe(3);
    expect(copy.fightSize).toBe(3);
    expect(copy.type).toBe(TournamentType.LEAGUE);
    expect(copy.duelsDuration).toBe(180);
    expect(copy.locked).toBeFalse();
    expect(tournament.scoreRules).toContain('HintHint');
    tournament.tournamentScore = undefined as unknown as Tournament['tournamentScore'];
    expect(tournament.scoreRules).toBe('');
  });
});
