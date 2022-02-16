export class Element {
  public id?: number;
  public createdAt: Date;

  public static copy(source: Element, target: Element): void {
    target.id = source.id;
    target.createdAt = source.createdAt;
  }

}
