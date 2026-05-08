import { Achievement } from './achievement.model';
import { Participant } from './participant';
import { Tournament } from './tournament';
import { TournamentScore } from './tournament-score.model';

describe('Achievement', () => {
  const buildParticipant = (): Participant => {
    const participant = new Participant();
    participant.id = 4;
    participant.name = 'Ken';
    participant.lastname = 'Do';
    participant.idCard = 'XYZ';
    return participant;
  };

  const buildTournament = (): Tournament => {
    const tournament = new Tournament();
    tournament.id = 5;
    tournament.name = 'Open';
    tournament.tournamentScore = new TournamentScore();
    return tournament;
  };

  const buildAchievement = (): Achievement => {
    const achievement = new Achievement();
    achievement.id = 1;
    achievement.participant = buildParticipant();
    achievement.tournament = buildTournament();
    achievement.achievementType = 'TYPE' as any;
    achievement.achievementGrade = 'GRADE' as any;
    return achievement;
  };

  it('should copy nested participant and tournament', () => {
    const source = buildAchievement();
    const target = new Achievement();

    Achievement.copy(source, target);

    expect(target.participant).toBeTruthy();
    expect(target.tournament).toBeTruthy();
    expect(target.participant).not.toBe(source.participant);
    expect(target.tournament).not.toBe(source.tournament);
    expect(target.achievementType).toBe('TYPE' as any);
    expect(target.achievementGrade).toBe('GRADE' as any);
  });

  it('should clone achievement into a new instance', () => {
    const source = buildAchievement();

    const clone = Achievement.clone(source);

    expect(clone).not.toBe(source);
    expect(clone.participant.id).toBe(4);
    expect(clone.tournament.id).toBe(5);
  });

  it('should handle missing nested entities in copy', () => {
    const source = new Achievement();
    const target = new Achievement();
    source.achievementType = 'TYPE' as any;
    source.achievementGrade = 'GRADE' as any;

    Achievement.copy(source, target);

    expect(target.participant).toBeUndefined();
    expect(target.tournament).toBeUndefined();
    expect(target.achievementType).toBe('TYPE' as any);
  });
});

