export class TemporalToken {
  public temporalToken: string;
  public temporalTokenExpirationTime?: Date;

  constructor(temporalToken: string) {
    this.temporalToken = temporalToken;
  }
}
