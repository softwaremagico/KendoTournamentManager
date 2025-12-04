export class Constants {


  public static readonly SESSION_STORAGE = class {
    public static readonly AUTH_TOKEN: string = 'jwt';
    public static readonly AUTH_EXPIRATION: string = 'jwt_expires';
    public static readonly USER: string = 'user';
  }

  public static readonly PATHS = class {
    public static readonly REGISTRY = class {
      public static readonly ROOT: string = 'registry';
      public static readonly CLUBS: string = 'clubs';
      public static readonly PARTICIPANTS: string = 'participants';
    }
    public static readonly TOURNAMENTS = class {
      public static readonly ROOT: string = 'tournaments';
    }
    public static readonly ADMINISTRATION = class {
      public static readonly ROOT: string = 'administration';
      public static readonly USERS: string = 'users';
    }
    public static readonly PROFILE = class {
      public static readonly ROOT: string = 'profile';
      public static readonly LANGUAGE: string = 'language';
      public static readonly PASSWORD: string = 'password';
      public static readonly LOGOUT: string = 'logout';
    }


    public static readonly QUERY = class {
      public static readonly EXPIRED: string = 'expired';
      public static readonly LOGOUT: string = 'logout';
    }
  }
}
