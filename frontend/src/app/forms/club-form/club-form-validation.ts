import {Club} from "../../models/club";
import {ClubFormValidationFields} from "./club-form-validation-fields";
import {InputLimits} from "../../utils/input-limits";
import {TranslocoService} from "@jsverse/transloco";
import {TypeValidations} from "../../utils/type-validations";

export function validateClubForm(club: Club, transloco: TranslocoService, errors: Map<ClubFormValidationFields, string>): boolean {
  return validateRequiredField(club.name, InputLimits.MIN_FIELD_LENGTH, InputLimits.MAX_NORMAL_FIELD_LENGTH, ClubFormValidationFields.NAME_ERRORS, transloco, errors)
    && validateRequiredField(club.country, InputLimits.MIN_FIELD_LENGTH, InputLimits.MAX_SMALL_FIELD_LENGTH, ClubFormValidationFields.COUNTRY_ERRORS, transloco, errors)
    && validateRequiredField(club.city, InputLimits.MIN_FIELD_LENGTH, InputLimits.MAX_SMALL_FIELD_LENGTH, ClubFormValidationFields.CITY_ERRORS, transloco, errors)
    && validateOptionalField(club.email, TypeValidations.isEmail, InputLimits.MAX_NORMAL_FIELD_LENGTH, ClubFormValidationFields.EMAIL_ERRORS, transloco, errors)
    && validateOptionalField(club.phone, TypeValidations.isPhoneNumber, InputLimits.MAX_SMALL_FIELD_LENGTH, ClubFormValidationFields.PHONE_ERRORS, transloco, errors, InputLimits.MIN_FIELD_LENGTH)
    && validateOptionalField(club.web, TypeValidations.isWebPage, InputLimits.MAX_NORMAL_FIELD_LENGTH, ClubFormValidationFields.WEB_ERRORS, transloco, errors, InputLimits.MIN_FIELD_LENGTH);
}

function validateRequiredField(value: string | undefined, minLength: number, maxLength: number, field: ClubFormValidationFields,
                               transloco: TranslocoService, errors: Map<ClubFormValidationFields, string>): boolean {
  if (!value || value.length === 0) {
    errors.set(field, transloco.translate(`v.dataIsMandatory`));
    return false;
  }
  if (value.length < minLength) {
    errors.set(field, transloco.translate(`v.minLengthError`));
    return false;
  }
  if (value.length > maxLength) {
    errors.set(field, transloco.translate(`v.maxLengthError`));
    return false;
  }
  return true;
}

function validateOptionalField(value: string | undefined, validator: (value: string) => boolean, maxLength: number,
                               field: ClubFormValidationFields, transloco: TranslocoService,
                               errors: Map<ClubFormValidationFields, string>, minLength: number = 0): boolean {
  if (!value) {
    return true;
  }
  return validatePresentOptionalField(value, validator, maxLength, field, transloco, errors, minLength);
}

function validatePresentOptionalField(value: string, validator: (value: string) => boolean, maxLength: number,
                                      field: ClubFormValidationFields, transloco: TranslocoService,
                                      errors: Map<ClubFormValidationFields, string>, minLength: number = 0): boolean {
  if (!validator(value)) {
    errors.set(field, transloco.translate(`v.formatInvalid`));
    return false;
  }
  if (minLength > 0 && value.length < minLength) {
    errors.set(field, transloco.translate(`v.minLengthError`));
    return false;
  }
  if (value.length > maxLength) {
    errors.set(field, transloco.translate(`v.maxLengthError`));
    return false;
  }
  return true;
}

