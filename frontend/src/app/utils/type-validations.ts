export class TypeValidations {

  public static isEmail(value: string): boolean {
    const trimmedValue: string = value?.trim() ?? '';
    if (!trimmedValue || trimmedValue.includes(' ')) {
      return false;
    }

    const atIndex: number = trimmedValue.indexOf('@');
    if (atIndex <= 0 || atIndex !== trimmedValue.lastIndexOf('@') || atIndex === trimmedValue.length - 1) {
      return false;
    }

    const localPart: string = trimmedValue.slice(0, atIndex);
    const domainPart: string = trimmedValue.slice(atIndex + 1);
    return this.isValidEmailLocalPart(localPart) && this.isValidDomain(domainPart);
  }

  public static isPhoneNumber(value: string): boolean {
    const trimmedValue: string = value?.trim() ?? '';
    if (!trimmedValue) {
      return false;
    }

    // Solo permite caracteres comunes de telefono.
    if (!/^[+\d\s()-]+$/.test(trimmedValue)) {
      return false;
    }

    // '+' opcional, unico y solo al inicio.
    const plusCount: number = (trimmedValue.match(/\+/g) || []).length;
    if (plusCount > 1 || (plusCount === 1 && !trimmedValue.startsWith('+'))) {
      return false;
    }

    // Requiere un numero realista de digitos para evitar falsos positivos.
    const digitsOnly: string = trimmedValue.replace(/\D/g, '');
    return digitsOnly.length >= 7 && digitsOnly.length <= 15;
  }

  public static isWebPage(value: string): boolean {
    const trimmedValue: string = value?.trim() ?? '';
    if (!trimmedValue || trimmedValue.includes(' ')) {
      return false;
    }

    const normalizedValue: string = trimmedValue.includes('://') ? trimmedValue : `https://${trimmedValue}`;

    try {
      const parsedUrl: URL = new URL(normalizedValue);
      return (parsedUrl.protocol === 'http:' || parsedUrl.protocol === 'https:')
        && !parsedUrl.username
        && !parsedUrl.password
        && this.isValidDomain(parsedUrl.hostname);
    } catch (_error) {
      return false;
    }
  }

  private static isValidEmailLocalPart(localPart: string): boolean {
    if (!localPart || localPart.startsWith('.') || localPart.endsWith('.') || localPart.includes('..')) {
      return false;
    }

    const allowedSpecialCharacters: string = ".!#$%&'*+/=?^_`{|}~-";
    for (const character of localPart) {
      if (!this.isAsciiLetter(character) && !this.isDigit(character) && !allowedSpecialCharacters.includes(character)) {
        return false;
      }
    }
    return true;
  }

  private static isValidDomain(domain: string): boolean {
    const normalizedDomain: string = domain.toLowerCase();
    if (!normalizedDomain || normalizedDomain.endsWith('.')) {
      return false;
    }

    const domainLabels: string[] = normalizedDomain.split('.');
    if (domainLabels.length < 2) {
      return false;
    }

    const topLevelDomain: string = domainLabels[domainLabels.length - 1];
    if (topLevelDomain.length < 2 || ![...topLevelDomain].every((character: string): boolean => this.isAsciiLetter(character))) {
      return false;
    }

    return domainLabels.every((label: string): boolean => this.isValidDomainLabel(label));
  }

  private static isValidDomainLabel(label: string): boolean {
    if (!label || label.startsWith('-') || label.endsWith('-')) {
      return false;
    }

    for (const character of label) {
      if (!this.isAsciiLetter(character) && !this.isDigit(character) && character !== '-') {
        return false;
      }
    }
    return true;
  }

  private static isAsciiLetter(character: string): boolean {
    const codePoint: number = character.codePointAt(0) ?? 0;
    return (codePoint >= 65 && codePoint <= 90) || (codePoint >= 97 && codePoint <= 122);
  }

  private static isDigit(character: string): boolean {
    const codePoint: number = character.codePointAt(0) ?? 0;
    return codePoint >= 48 && codePoint <= 57;
  }
}
