export class Club {
  public id?: number;
  public name: string;
  public country?: string;
  public city?: string;
  public address?: string;
  public email?: string;
  public phone?: string;
  public web?: string;

  public static copy(source: Club, target: Club): void {
    target.id = source.id;
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
