export enum Score {
  MEN = 'MEN',
  KOTE = 'KOTE',
  DO = 'DO',
  TSUKI = 'TSUKI',
  IPPON = 'IPPON',
  HANSOKU = 'HANSOKU',
  EMPTY = 'EMPTY'
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
    return Object.keys(Score).filter(enumValue => (typeof (Score[enumValue as Score]) !== 'function'))
  }
}

export namespace Score {
  export function toArray(): Score[] {
    return Score.getKeys().map(key => {
      return <Score>(<any>Score)[key];
    });
  }
}

export namespace Score {
  export function noCompetitor(): Score[] {
    const scores: Score[] = [];
    scores.push(Score.IPPON);
    scores.push(Score.EMPTY);
    return scores;
  }
}

export namespace Score {
  export function clear(): Score[] {
    const scores: Score[] = [];
    scores.push(Score.EMPTY);
    return scores;
  }
}

export namespace Score {
  export function getEnumKeyByEnumValue<T extends { [index: string]: string }>(myEnum: T, enumValue: string): keyof T | null {
    let keys = Object.keys(myEnum).filter(x => myEnum[x] == enumValue);
    return keys.length > 0 ? keys[0] : null;
  }
}

export namespace Score {
  export function tag(score: Score): string {
    if (score) {
      switch (score) {
        case Score.MEN:
          return "M";
        case Score.KOTE:
          return "K";
        case Score.DO:
          return "D";
        case Score.TSUKI:
          return "T";
        case Score.HANSOKU:
          return "H";
        case Score.IPPON:
          return "I";
      }
    }
    return "";
  }
}

export namespace Score {
  export function label(score: Score): string {
    return score[0].toUpperCase() + score.substring(1).toLowerCase();
  }
}
