import {UserRoles} from "../services/rbac/user-roles";
import {DatabaseObject} from "./database-object";

export class AuthenticatedUser extends DatabaseObject {
  public username: string;
  public password: string;
  public name: string;
  public lastname: string;
  public jwt: string;
  public expires: number;
  public roles: UserRoles[];

  constructor() {
    super();
    this.roles = [];
  }
}
