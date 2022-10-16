export class Element {
  public id?: number;
  public createdAt: Date;
  public updatedAt?: Date;
  public createdBy?: string;
  public updatedBy?: string;

  constructor() {
    this.createdAt = new Date();
    this.createdBy = localStorage.getItem('username')!;
  }


  public static copy(source: Element, target: Element): void {
    target.id = source.id;
    target.createdAt = source.createdAt;
    target.updatedAt = source.updatedAt;
    target.createdBy = source.createdBy;
    target.updatedBy = source.updatedBy;
  }

}
