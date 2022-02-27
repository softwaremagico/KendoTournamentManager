export enum Score {
  MEN = 'MEN',
  KOTE = 'KOTE',
  DO = 'DO',
  TSUKI = 'TSUKI',
  IPPON = 'IPPON',
  HANSOKU = 'HANSOKU',
  EMPTY = 'EMPTY',
  DRAW = 'DRAW'
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

export namespace Score {
  export function getKeys(): string[] {
    return Object.keys(Score).filter(enumValue => !(typeof (Score[enumValue as Score]) === 'function'))
  }
}

export namespace Score {
  export function toArray(): Score[] {
    return Score.getKeys().map(key => {
      return <Score>(<any>Score)[key];
    });
  }
}
