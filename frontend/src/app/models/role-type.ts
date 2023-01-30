export enum RoleType {
  COMPETITOR = 'COMPETITOR',
  REFEREE = 'REFEREE',
  VOLUNTEER = 'VOLUNTEER'
}

export namespace RoleType {
  export function getByKey(key: string): string {
    for (const valueKey in RoleType) {
      if ((RoleType as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return RoleType.COMPETITOR;
  }
}

export namespace RoleType {
  export function getKeys(): string[] {
    return Object.keys(RoleType).filter(enumValue => (typeof (RoleType[enumValue as RoleType]) !== 'function'))
  }
}

export namespace RoleType {
  export function getRandom(): RoleType {
    const values = getKeys();
    const enumKey = values[Math.floor(Math.random() * values.length)];
    return <RoleType>(<any>RoleType)[enumKey];
  }
}

export namespace RoleType {
  export function toArray(): RoleType[] {
    return RoleType.getKeys().map(key => {
      return <RoleType>(<any>RoleType)[key];
    });
  }
}
