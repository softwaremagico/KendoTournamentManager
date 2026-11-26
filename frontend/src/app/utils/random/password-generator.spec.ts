import {Constants} from '../../constants';
import {PasswordGenerator} from './password-generator';

describe('PasswordGenerator', () => {
  it('should generate a password of the expected length using Web Crypto', () => {
    const password = PasswordGenerator.generate();

    expect(password.length).toBeGreaterThanOrEqual(Constants.PASSWORDS.MIN_LENGTH);
    expect(password.length).toBeLessThanOrEqual(Constants.PASSWORDS.MAX_LENGTH);
  });

  it('should fall back to Math.random when Web Crypto is unavailable', () => {
    spyOnProperty(globalThis, 'crypto', 'get').and.returnValue(undefined as any);

    const password = PasswordGenerator.generate();

    expect(password.length).toBeGreaterThanOrEqual(Constants.PASSWORDS.MIN_LENGTH);
  });
});
