import {Duel} from '../../models/duel';
import {ScoreUpdatedService} from './score-updated.service';

describe('ScoreUpdatedService', () => {
  it('starts with a duel and publishes score updates', () => {
    const service = new ScoreUpdatedService();
    expect(service.isScoreUpdated.value).toEqual(jasmine.any(Duel));
    const update = new Duel();
    service.isScoreUpdated.next(update);
    expect(service.isScoreUpdated.value).toBe(update);
  });
});
