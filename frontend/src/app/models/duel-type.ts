export enum DuelType {
  STANDARD = 'STANDARD',
  UNDRAW = 'UNDRAW'
}

export namespace DuelType {
  export function getByKey(key: string) {
    for (const valueKey in DuelType) {
      if ((DuelType as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }
}

export namespace DuelType {
  export function getKeys(): string[] {
    return Object.keys(DuelType).filter(enumValue => !(typeof (DuelType[enumValue as DuelType]) === 'function'))
  }
}

export namespace DuelType {
  export function toArray(): DuelType[] {
    return DuelType.getKeys().map(key => {
      return <DuelType>(<any>DuelType)[key];
    });
  }
}
