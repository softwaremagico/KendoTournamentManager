import {UserRoles} from "../services/rbac/user-roles";

export class AuthenticatedUser {
  public id?: number;
  public username: string;
  public password: string;
  public name: string;
  public lastname: string;
  public jwt: string;
  public expires: number;
  public roles: UserRoles[];

  constructor() {
    this.roles = [];
  }
}
