import {Element} from "./element";

export class Club extends Element {
  public name: string;
  public country?: string;
  public city?: string;
  public address?: string;
  public email?: string;
  public phone?: string;
  public web?: string;

  constructor() {
    super();
  }

  public static override copy(source: Club, target: Club): void {
    Element.copy(source, target);
    target.name = source.name;
    target.country = source.country;
    target.city = source.city;
    target.address = source.address;
    target.email = source.email;
    target.phone = source.phone;
    target.web = source.web;
  }

  public static clone(data: Club): Club {
    const instance: Club = new Club();
    this.copy(data, instance);
    return instance;
  }
}
