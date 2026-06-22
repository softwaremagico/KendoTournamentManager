import {random} from "../utils/random/random";

export enum RoleType {
  COMPETITOR = 'COMPETITOR',
  REFEREE = 'REFEREE',
  ORGANIZER = 'ORGANIZER',
  VOLUNTEER = 'VOLUNTEER',
  PRESS = 'PRESS'
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
    const enumKey = values[Math.floor(random() * values.length)];
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
