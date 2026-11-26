import {ScoreOfCompetitor} from './score-of-competitor';
import {Participant} from './participant';

describe('ScoreOfCompetitor', () => {
  it('copies the recorded counters into a distinct instance', () => {
    const source = new ScoreOfCompetitor();
    source.wonDuels = 2;
    source.drawDuels = 1;
    source.hits = 5;
    source.hitsLost = 3;
    source.duelsDone = 4;
    source.wonFights = 2;
    source.drawFights = 1;
    source.totalFights = 3;
    const copy = ScoreOfCompetitor.clone(source);
    expect(copy).not.toBe(source);
    expect(copy.wonDuels).toBe(2);
    expect(copy.hits).toBe(5);
    expect(copy.totalFights).toBe(3);
    expect(copy.competitor).toBeUndefined();
  });

  it('clones the competitor rather than sharing its reference', () => {
    const source = new ScoreOfCompetitor();
    source.competitor = new Participant();
    source.competitor.name = 'Aki';
    const copy = ScoreOfCompetitor.clone(source);
    expect(copy.competitor).not.toBe(source.competitor);
    expect(copy.competitor.name).toBe('Aki');
  });
});
