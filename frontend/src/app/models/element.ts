import {DatabaseObject} from "./database-object";

export class Element extends DatabaseObject {
  public createdAt: Date;
  public updatedAt?: Date;
  public createdBy?: string;
  public updatedBy?: string;

  constructor() {
    super();
    this.createdAt = new Date();
    this.createdBy = localStorage.getItem('username')!;
  }


  public static override copy(source: Element, target: Element): void {
    super.copy(source, target);
    target.createdAt = source.createdAt;
    target.updatedAt = source.updatedAt;
    target.createdBy = source.createdBy;
    target.updatedBy = source.updatedBy;
  }

}
