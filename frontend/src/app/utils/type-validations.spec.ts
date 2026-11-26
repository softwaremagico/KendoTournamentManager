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
      expect(TypeValidations.isEmail(' club@example.com')).toBeTrue();
      expect(TypeValidations.isEmail('club name@example.com')).toBeFalse();
      expect(TypeValidations.isEmail('.club@example.com')).toBeFalse();
      expect(TypeValidations.isEmail('club.@example.com')).toBeFalse();
      expect(TypeValidations.isEmail('cl..ub@example.com')).toBeFalse();
      expect(TypeValidations.isEmail('clubñ@example.com')).toBeFalse();
      expect(TypeValidations.isEmail('club@example.c')).toBeFalse();
      expect(TypeValidations.isEmail('club@example.com.')).toBeFalse();
      expect(TypeValidations.isEmail('club@-example.com')).toBeFalse();
      expect(TypeValidations.isEmail('club@' + 'a'.repeat(70) + '.com')).toBeTrue();
    });
  });

  describe('isPhoneNumber', () => {
    it('accepts international, spaced and dashed numbers', () => {
      expect(TypeValidations.isPhoneNumber('+34 600 123 456')).toBeTrue();
      expect(TypeValidations.isPhoneNumber('600-123-456')).toBeTrue();
      expect(TypeValidations.isPhoneNumber('(600) 123456')).toBeTrue();
    });

    it('rejects missing, malformed or repeated plus signs', () => {
      expect(TypeValidations.isPhoneNumber('')).toBeFalse();
      expect(TypeValidations.isPhoneNumber('600abc123')).toBeFalse();
      expect(TypeValidations.isPhoneNumber('++34600123456')).toBeFalse();
      expect(TypeValidations.isPhoneNumber('34+600123456')).toBeFalse();
      expect(TypeValidations.isPhoneNumber('12345')).toBeFalse();
      expect(TypeValidations.isPhoneNumber('1234567890123456')).toBeFalse();
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

