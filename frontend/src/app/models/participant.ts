import {Club} from "./club";

export class Participant {
  public id?: number;
  public idCard: string;
  public name: string;
  public lastname: string;
  public club?: Club;

  public get clubName(): string {
    return this.club ? this.club.name : "";
  }

  public static copy(source: Participant, target: Participant): void {
    target.id = source.id;
    target.idCard = source.idCard;
    target.name = source.name;
    target.lastname = source.lastname;
    if (source.club !== undefined) {
      target.club = Club.clone(source.club);
    }
  }

  public static clone(data: Participant): Participant {
    const instance: Participant = new Participant();
    this.copy(data, instance);
    return instance;
  }
}
