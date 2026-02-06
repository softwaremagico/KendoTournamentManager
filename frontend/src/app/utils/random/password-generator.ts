import {Constants} from "../../constants";

export class PasswordGenerator {
  public static generate(): string {
    const pattern: RegExp = /[A-Za-z\d@$!%*?&]/
    const randomSize: number = Math.floor(Math.random() * (Constants.PASSWORDS.MAX_LENGTH - Constants.PASSWORDS.MIN_LENGTH + 1))
      + Constants.PASSWORDS.MIN_LENGTH;
    let password: string = '';
    while (password.length < randomSize) {
      const result: string = String.fromCharCode(this.randomChar());
      if (pattern.test(result)) {
        password += result;
      }
    }
    return password;
  }

  private static randomChar(): number {
    if (window.crypto && window.crypto.getRandomValues) {
      const buffer: Uint8Array = new Uint8Array(1);
      window.crypto.getRandomValues(buffer);
      return buffer[0];
    } else {
      return Math.floor(Math.random() * 256);
    }
  }
}
