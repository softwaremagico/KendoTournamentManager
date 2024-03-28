export class TemporalToken {
  public content: string;
  public temporalTokenExpirationTime?: Date;

  constructor(content: string) {
    this.content = content;
  }
}
