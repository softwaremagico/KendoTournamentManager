import {UserRoles} from "../services/rbac/user-roles";
import {DatabaseObject} from "./database-object";
import {Element} from "./element";

export class AuthenticatedUser extends Element {
  public username: string;
  public password: string;
  public name: string;
  public lastname: string;
  public jwt: string;
  public expires: number;
  public roles: UserRoles[];
  //Session is obtained from X-Session header when log-in.
  public session: string;

  constructor() {
    super();
    this.roles = [];
  }

  public static override copy(from: AuthenticatedUser, to: AuthenticatedUser): void {
    Element.copy(from, to);
    to.username = from.username;
    to.password = from.password;
    to.name = from.name;
    to.lastname = from.lastname;
    to.jwt = from.jwt;
    to.expires = from.expires;
    to.roles = from.roles;
    to.session = from.session;
  }

  public static clone(from: AuthenticatedUser): AuthenticatedUser {
    const to: AuthenticatedUser = new AuthenticatedUser();
    AuthenticatedUser.copy(from, to);
    return to;
  }
}
