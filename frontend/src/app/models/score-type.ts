export enum ScoreType {
  CLASSIC = 'CLASSIC',
  //WIN_OVER_DRAWS = 'WIN_OVER_DRAWS',
  EUROPEAN = 'EUROPEAN',
  CUSTOM = 'CUSTOM',
  INTERNATIONAL = 'INTERNATIONAL'
}

export namespace Score {
  export function getByKey(key: string) {
    for (const valueKey in Score) {
      if ((Score as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }
}

export namespace ScoreType {
  export function getKeys(): string[] {
    return Object.keys(ScoreType).filter(enumValue => (typeof (ScoreType[enumValue as ScoreType]) !== 'function'))
  }
}

export namespace ScoreType {
  export function toArray(): ScoreType[] {
    return ScoreType.getKeys().map(key => {
      return <ScoreType>(<any>ScoreType)[key];
    });
  }
}

export namespace ScoreType {
  export function getEnumKeyByEnumValue<T extends { [index: string]: string }>(myEnum: T, enumValue: string): keyof T | null {
    let keys = Object.keys(myEnum).filter(x => myEnum[x] == enumValue);
    return keys.length > 0 ? keys[0] : null;
  }
}

