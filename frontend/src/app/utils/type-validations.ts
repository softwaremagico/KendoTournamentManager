export class TypeValidations {

  public static isEmail(value: string): boolean {
    // Validacion de email simple y segura frente a backtracking excesivo.
    return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(value);
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
    return new RegExp('(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?').test(value);
  }
}
