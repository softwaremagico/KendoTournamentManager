export enum RoleType {
  REFEREE = 'REFEREE',
  COMPETITOR = 'COMPETITOR',
  VOLUNTEER = 'VOLUNTEER'
}

export namespace RoleType {
  export function getByKey(key: string) {
    for (const valueKey in RoleType) {
      if ((RoleType as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }
}
