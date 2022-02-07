export enum RoleType {
  COMPETITOR = 'COMPETITOR',
  REFEREE = 'REFEREE',
  VOLUNTEER = 'VOLUNTEER'
}

export namespace RoleType {
  export function getByKey(key: string): string | undefined {
    for (const valueKey in RoleType) {
      if ((RoleType as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }
}

export namespace RoleType {
  export function getKeys(): string[] {
    return Object.keys(RoleType).filter(enumValue => !(typeof (RoleType[enumValue as RoleType]) === 'function'))
  }
}
