export enum ScoreType {
  CLASSIC = 'CLASSIC',
  WIN_OVER_DRAWS = 'WIN_OVER_DRAWS',
  EUROPEAN = 'EUROPEAN',
  INTERNATIONAL = 'INTERNATIONAL',
  CUSTOM = 'CUSTOM',
}

export namespace ScoreType {
  export function getByKey(key: string) {
    for (const valueKey in ScoreType) {
      if ((ScoreType as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }

  export function getKeys(): string[] {
    return Object.keys(ScoreType).filter(enumValue => (typeof (ScoreType[enumValue as ScoreType]) !== 'function'))
  }

  export function toArray(): ScoreType[] {
    return getKeys().map(key => {
      return <ScoreType>(<any>ScoreType)[key];
    });
  }

  export function toCamel(scoreType: ScoreType) {
    return scoreType.toLowerCase()
      .replace(/_(.)/g, function ($1) {
        return $1.toUpperCase();
      })
      .replace(/_/g, '');
  }

  export function getEnumKeyByEnumValue<T extends {
    [index: string]: string
  }>(myEnum: T, enumValue: string): keyof T | null {
    let keys = Object.keys(myEnum).filter(x => myEnum[x] == enumValue);
    return keys.length > 0 ? keys[0] : null;
  }
}

