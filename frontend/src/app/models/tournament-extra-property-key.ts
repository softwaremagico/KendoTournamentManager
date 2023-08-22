export enum TournamentExtraPropertyKey {
  MAXIMIZE_FIGHTS = 'MAXIMIZE_FIGHTS',
  KING_INDEX = 'KING_INDEX',
  KING_DRAW_RESOLUTION = 'KING_DRAW_RESOLUTION',
  DIPLOMA_NAME_HEIGHT = 'DIPLOMA_NAME_HEIGHT',
  NUMBER_OF_WINNERS = 'NUMBER_OF_WINNERS'
}

export namespace TournamentExtraPropertyKey {
  export function getByKey(key: string) {
    for (const valueKey in TournamentExtraPropertyKey) {
      if ((TournamentExtraPropertyKey as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }
}

export namespace TournamentExtraPropertyKey {
  export function getKeys(): string[] {
    return Object.keys(TournamentExtraPropertyKey).filter(enumValue => (typeof (TournamentExtraPropertyKey[enumValue as TournamentExtraPropertyKey]) !== 'function'))
  }
}

export namespace TournamentExtraPropertyKey {
  export function toArray(): TournamentExtraPropertyKey[] {
    return TournamentExtraPropertyKey.getKeys().map(key => {
      return <TournamentExtraPropertyKey>(<any>TournamentExtraPropertyKey)[key];
    });
  }
}

export namespace TournamentExtraPropertyKey {
  export function getEnumKeyByEnumValue<T extends {
    [index: string]: string
  }>(myEnum: T, enumValue: string): keyof T | null {
    let keys = Object.keys(myEnum).filter(x => myEnum[x] == enumValue);
    return keys.length > 0 ? keys[0] : null;
  }
}

