export class AuthRequest {
  public username: string;
  public password: string;
  public tenant: string;

  constructor(username: string, password: string, tenant: string) {
    this.username = username;
    this.password = password;
    this.tenant = tenant;
  }
}
