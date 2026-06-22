export enum UserRoles {
  VIEWER = 'VIEWER',
  EDITOR = 'EDITOR',
  ADMIN = 'ADMIN',
  GUEST = 'GUEST',
}

export namespace UserRoles {
  export function getByKey(key: string): UserRoles | undefined {
    for (const valueKey in UserRoles) {
      if ((UserRoles as any)[valueKey] === key.toUpperCase()) {
        return <UserRoles>valueKey;
      }
    }
    return undefined;
  }
}

export namespace UserRoles {
  export function getByKeys(keys: string[]): UserRoles[] {
    const userRoles: UserRoles[] = [];
    for (const key of keys) {
      const userRole: UserRoles | undefined = getByKey(key);
      if (userRole) {
        userRoles.push(userRole);
      }
    }
    return userRoles;
  }
}

export namespace UserRoles {
  export function getKeys(): string[] {
    return Object.keys(UserRoles).filter(enumValue => (typeof (UserRoles[enumValue as UserRoles]) !== 'function'))
  }
}

export namespace UserRoles {
  export function toArray(): UserRoles[] {
    return UserRoles.getKeys().map(key => {
      return <UserRoles>(<any>UserRoles)[key];
    });
  }
}
