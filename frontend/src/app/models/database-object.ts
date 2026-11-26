export class DatabaseObject {
  public id?: number;


  public static copy(source: DatabaseObject, target: DatabaseObject): void {
    target.id = source.id;
  }

}
