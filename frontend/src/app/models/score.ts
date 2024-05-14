export enum Score {
  MEN = 'MEN',
  KOTE = 'KOTE',
  DO = 'DO',
  TSUKI = 'TSUKI',
  IPPON = 'IPPON',
  FUSEN_GACHI = 'FUSEN_GACHI',
  HANSOKU = 'HANSOKU',
  EMPTY = 'EMPTY'
}

export namespace Score {
  export function getByKey(key: string): string | undefined {
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
    return Object.keys(Score).filter((enumValue: string): boolean => (typeof (Score[enumValue as Score]) !== 'function'))
  }

  export function toArray(): Score[] {
    return Score.getKeys().map((key: string) => {
      return <Score>(<any>Score)[key];
    });
  }
}

export namespace Score {
  export function noCompetitor(): Score[] {
    const scores: Score[] = [];
    scores.push(Score.FUSEN_GACHI);
    scores.push(Score.FUSEN_GACHI);
    return scores;
  }

  export function clear(): Score[] {
    const scores: Score[] = [];
    scores.push(Score.EMPTY);
    return scores;
  }

  export function getEnumKeyByEnumValue<T extends {
    [index: string]: string
  }>(myEnum: T, enumValue: string): keyof T | null {
    let keys: string[] = Object.keys(myEnum).filter((x: string): boolean => myEnum[x] == enumValue);
    return keys.length > 0 ? keys[0] : null;
  }

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
        case Score.FUSEN_GACHI:
          return " ";
      }
    }
    return "";
  }

  export function label(score: Score): string {
    return score[0].toUpperCase() + score.substring(1).toLowerCase();
  }


  export function toCamel(score: Score): string {
    return score.toLowerCase()
      .replace(/_(.)/g, function ($1: string) {
        return $1.toUpperCase();
      })
      .replace(/_/g, '');
  }
}
