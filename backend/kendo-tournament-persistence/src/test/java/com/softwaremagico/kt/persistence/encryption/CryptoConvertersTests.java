package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "cryptoConverters")
public class CryptoConvertersTests {

	private static final String PASSWORD = "myEncryptionCode";
	private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA36J7OzYH4rXRXKSYa4tSUKMv70zxmA23Bd9O/+xKIVugQk9B3DtjZqUHqL2f5XcvTWu6diwFlGgkGhOjl9iHO/k5Vu9GG0MJ1gB75bbW1vWMu5wiw5lipwID2sI/liTxNFTo462oG1PR35weEuTgboftjRwKoGXWTBigyVYDS4kqUh5N5V8AeerGyFRv9OX6oEGQK8UQZQQ0O3ogb0A8GuAxVO8vdEei59uv8VqGVVSROYbfV+X/8Lz5xCNr73XxTnJAI8LnrBCnXeBIThlx31usnvc/IacojGBZgyEv0S+1D+zJIwyTE/60mN845bHhMBNStRqvGjnNFaAdacWavQIDAQAB";
	private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDfons7NgfitdFcpJhri1JQoy/vTPGYDbcF307/7EohW6BCT0HcO2NmpQeovZ/ldy9Na7p2LAWUaCQaE6OX2Ic7+TlW70YbQwnWAHvlttbW9Yy7nCLDmWKnAgPawj+WJPE0VOjjragbU9HfnB4S5OBuh+2NHAqgZdZMGKDJVgNLiSpSHk3lXwB56sbIVG/05fqgQZArxRBlBDQ7eiBvQDwa4DFU7y90R6Ln26/xWoZVVJE5ht9X5f/wvPnEI2vvdfFOckAjwuesEKdd4EhOGXHfW6ye9z8hpyiMYFmDIS/RL7UP7MkjDJMT/rSY3zjlseEwE1K1Gq8aOc0VoB1pxZq9AgMBAAECggEAM8aFa1b+CmH339AHrdZqr1qorWmOAZsjRYvG2X+WRhTVjKSW5esl/0yz8kA8tb6bR/xqBhP5Ervtp8/mLzClVqBDwznQbF1f+JjSFoP3R3tAIutqLQjlCy9vPwV7/UH+ShRietvosLL+Qxv85njw8QYtXF8iorIRJDsLdrGgP6tKFIRjFdHqevQYeaUwrhOXDzSPRXy3SCzLdRtJ7WOsTDIA9E4ilW0RG3McHWO5TiEkuQc6z7jzk/+ikgwn9+gCpGAmns7kISKva/b4bVKOqxgqUzcdpOJCTMU93Ai2msfa441QhPAXprCMkTIGkXDUQaKzUEfI7kpqTh0vBTgHLQKBgQD9YpDYdI6xhq3kJS4aF1Dn6/vN6VsRy7nxSqU2kDLKisBjiSGRuKfnGHPUWF9tK+V0rDt9c+U2d3LsQpDqt44Mih13HQRLZoknLwE/yC68jKUxWZjfw+yUm5LBnDUD0tVWl3ntOqbzMOFQG+v8sNJEVKYy0Vblo+F+DBYTpKkrswKBgQDh8VD2Uq4e2Z3GPvgREtfIb8/h6SCSNuQOgQZUz9uGQBhLFTqck32+iOTE5W5SpcG8kFUNg6I/aRJU4WBk+TWl09tkB+IcPu95UnKj9en3UM8f4qjZJhYbFeD4AC/vquVd49YD1vmD5msZ2a+ZWXIIoMvpqcYYSY+FX4/KDeEnzwKBgQD4HITyvydepB8wMEv1VnFtt8Cof2Eiite3cz1VsVtawwTiMkxBsYwPmP0Kp8JBV2NmnKPgExUWAV7yA7h9bWFV3ARAN5SV9IBGcxuJBg7lp9619SLhsaq+VZwdo0SySseF8+t7JRyHM4zc8YphtA8gw7a+OggZ8yABWfpgH7NbSwKBgFn8FueTdD9bIigTag66t2/fgCaUB2HUNK3GGzRp+B8WpdY5ZTd40kqxEJOOYIrP80HbJdSVCWhBu4hMnmx4iDI8FxNWxGnFcvCZsoi0ZvMVnvgu+FpLfeBZrVh4Ep9cK69iDxlGY+dxPFR2SyZC5lSjXbL456lCmsNelNC/CF4nAoGARtIxCA8tHyfCqgjMDQ+Y1ZE3sbynhUQvb/oTuz5Jsu7h8FzqlNDRvOaTppaZvDFhVxNdRSueynukeiepBlUJBow34qt22v1hAxQl0s5Qutni15ZXdIT24y9ESVvaM/VOxy2eHsGH8W7PM3i2s+yJYKSFKLGj+6eGM+MkvuMUa3A=";

	private FloatCryptoConverter floatConverter;
	private BooleanCryptoConverter booleanConverter;

	@BeforeClass
	public void setup() {
		new KeyProperty(PASSWORD, PUBLIC_KEY, PRIVATE_KEY);
		floatConverter = new FloatCryptoConverter();
		booleanConverter = new BooleanCryptoConverter();
	}

	// FloatCryptoConverter tests
	@Test
	public void testFloatConverterWithNull() {
		Assert.assertNull(floatConverter.convertToDatabaseColumn(null));
	}

	@Test
	public void testFloatConverterWithValue() {
		final Float originalValue = 123.45f;
		final String encrypted = floatConverter.convertToDatabaseColumn(originalValue);
		Assert.assertNotNull(encrypted);
	}

	@Test
	public void testFloatConverterRoundTrip() {
		final Float originalValue = 987.65f;
		final String encrypted = floatConverter.convertToDatabaseColumn(originalValue);
		final Float decrypted = floatConverter.convertToEntityAttribute(encrypted);
		Assert.assertNotNull(decrypted);
		// Note: Float comparison with precision
		Assert.assertEquals(decrypted, originalValue, 0.001f);
	}

	@Test
	public void testFloatConverterWithZero() {
		final Float originalValue = 0f;
		final String encrypted = floatConverter.convertToDatabaseColumn(originalValue);
		final Float decrypted = floatConverter.convertToEntityAttribute(encrypted);
		Assert.assertEquals(decrypted, originalValue);
	}

	@Test
	public void testFloatConverterWithNegativeValue() {
		final Float originalValue = -456.78f;
		final String encrypted = floatConverter.convertToDatabaseColumn(originalValue);
		final Float decrypted = floatConverter.convertToEntityAttribute(encrypted);
		Assert.assertEquals(decrypted, originalValue, 0.001f);
	}

	@Test
	public void testFloatConverterWithEmptyString() {
		final Float result = floatConverter.convertToEntityAttribute("");
		Assert.assertNull(result);
	}

	@Test
	public void testFloatConverterWithNullString() {
		final Float result = floatConverter.convertToEntityAttribute(null);
		Assert.assertNull(result);
	}

	@Test
	public void testFloatConverterWithInvalidString() {
		final Float result = floatConverter.convertToEntityAttribute("not_a_float");
		Assert.assertNull(result);
	}

	// BooleanCryptoConverter tests
	@Test
	public void testBooleanConverterWithNull() {
		Assert.assertNull(booleanConverter.convertToDatabaseColumn(null));
	}

	@Test
	public void testBooleanConverterWithTrue() {
		final Boolean originalValue = true;
		final String encrypted = booleanConverter.convertToDatabaseColumn(originalValue);
		Assert.assertNotNull(encrypted);
	}

	@Test
	public void testBooleanConverterWithFalse() {
		final Boolean originalValue = false;
		final String encrypted = booleanConverter.convertToDatabaseColumn(originalValue);
		Assert.assertNotNull(encrypted);
	}

	@Test
	public void testBooleanConverterRoundTripTrue() {
		final Boolean originalValue = true;
		final String encrypted = booleanConverter.convertToDatabaseColumn(originalValue);
		final Boolean decrypted = booleanConverter.convertToEntityAttribute(encrypted);
		Assert.assertEquals(decrypted, originalValue);
	}

	@Test
	public void testBooleanConverterRoundTripFalse() {
		final Boolean originalValue = false;
		final String encrypted = booleanConverter.convertToDatabaseColumn(originalValue);
		final Boolean decrypted = booleanConverter.convertToEntityAttribute(encrypted);
		Assert.assertEquals(decrypted, originalValue);
	}

	@Test
	public void testBooleanConverterWithEmptyString() {
		final Boolean result = booleanConverter.convertToEntityAttribute("");
		Assert.assertNull(result);
	}

	@Test
	public void testBooleanConverterWithNullString() {
		final Boolean result = booleanConverter.convertToEntityAttribute(null);
		Assert.assertNull(result);
	}

	@Test
	public void testBooleanConverterDifferentEncryptions() {
		final Boolean originalValue1 = true;
		final Boolean originalValue2 = false;
		final String encrypted1 = booleanConverter.convertToDatabaseColumn(originalValue1);
		final String encrypted2 = booleanConverter.convertToDatabaseColumn(originalValue2);
		Assert.assertNotEquals(encrypted1, encrypted2);
	}
}
