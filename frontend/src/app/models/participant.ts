import {Club} from "./club";
import {Element} from "./element";

export class Participant extends Element {
  public idCard: string;
  public name: string;
  public lastname: string;
  public club?: Club;
  public locked: boolean = false;

  public get clubName(): string {
    return this.club ? this.club.name : "";
  }

  public static override copy(source: Participant, target: Participant): void {
    Element.copy(source, target);
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
