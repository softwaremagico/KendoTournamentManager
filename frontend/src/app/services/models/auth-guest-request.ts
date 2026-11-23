export class AuthGuestRequest {
  public tournamentId: number;
  public ticket?: string;

  constructor(tournamentId: number, ticket?: string) {
    this.tournamentId = tournamentId;
    this.ticket = ticket;
  }
}
