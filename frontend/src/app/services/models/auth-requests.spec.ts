import {AuthGuestRequest} from './auth-guest-request';
import {TemporalToken} from './temporal-token';

describe('Authentication request models', () => {
  it('preserves the tournament and optional signed ticket', () => {
    const request = new AuthGuestRequest(7, 'signed-ticket');
    expect(request.tournamentId).toBe(7);
    expect(request.ticket).toBe('signed-ticket');
    expect(new AuthGuestRequest(9).ticket).toBeUndefined();
  });

  it('stores a temporal token', () => {
    expect(new TemporalToken('token-value').content).toBe('token-value');
  });
});
