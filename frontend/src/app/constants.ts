export class Constants {


  public static readonly SESSION_STORAGE = class {
    public static readonly AUTH_TOKEN: string = 'authToken';
    public static readonly AUTH_EXPIRATION: string = 'authExp';
    public static readonly USER: string = 'user';
    public static readonly PERMISSIONS: string = 'permissions';
  }

  public static readonly PATHS = class {
    public static readonly TOURNAMENTS: string = 'tournaments';

    public static readonly QUERY = class {
      public static readonly EXPIRED: string = 'expired';
      public static readonly LOGOUT: string = 'logout';
    }
  }
}
