import {Club} from "./club";

export class Participant {
  public id?: number;
  public idCard: string;
  public name: string;
  public lastname: string;
  public club?: Club;
}
