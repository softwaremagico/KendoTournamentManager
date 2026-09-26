import {TypeValidations} from "./type-validations";

describe('TypeValidations', () => {
  describe('isEmail', () => {
    it('returns true for valid emails', () => {
      expect(TypeValidations.isEmail('club@example.com')).toBeTrue();
      expect(TypeValidations.isEmail('club+test@example.co.uk')).toBeTrue();
    });

    it('returns false for malformed emails', () => {
      expect(TypeValidations.isEmail('clubexample.com')).toBeFalse();
      expect(TypeValidations.isEmail('club@@example.com')).toBeFalse();
      expect(TypeValidations.isEmail('club@invalid-domain')).toBeFalse();
    });
  });

  describe('isWebPage', () => {
    it('returns true for valid http and https urls', () => {
      expect(TypeValidations.isWebPage('https://example.com/path')).toBeTrue();
      expect(TypeValidations.isWebPage('example.com')).toBeTrue();
    });

    it('returns false for unsupported or malformed urls', () => {
      expect(TypeValidations.isWebPage('ftp://example.com')).toBeFalse();
      expect(TypeValidations.isWebPage('invalid domain')).toBeFalse();
      expect(TypeValidations.isWebPage('http://-example.com')).toBeFalse();
    });
  });
});

