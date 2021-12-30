import {Club} from "./club";

export class User {
  public id?: number;
  public idCard: string;
  public name: string;
  public lastname: string;
  public club?: Club;
}
