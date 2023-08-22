export enum TournamentType {
  CHAMPIONSHIP = 'CHAMPIONSHIP',
  LEAGUE = 'LEAGUE',
  LOOP = 'LOOP',
  // CUSTOM_CHAMPIONSHIP = 'CUSTOM_CHAMPIONSHIP',
  KING_OF_THE_MOUNTAIN = 'KING_OF_THE_MOUNTAIN',
  CUSTOMIZED = 'CUSTOMIZED'
}

export namespace TournamentType {
  export function getByKey(key: string): TournamentType | undefined {
    for (const valueKey in TournamentType) {
      if ((TournamentType as any)[valueKey] === key) {
        return (TournamentType as any)[valueKey];
      }
    }
    return undefined;
  }
}

export namespace TournamentType {
  export function getKeys(): string[] {
    return Object.keys(TournamentType).filter(enumValue => (typeof (TournamentType[enumValue as TournamentType]) !== 'function'))
  }
}

export namespace TournamentType {
  export function toArray(): TournamentType[] {
    return TournamentType.getKeys().map(key => {
      return <TournamentType>(<any>TournamentType)[key];
    });
  }
}

export namespace TournamentType {
  export function toCamel(tournamentType: TournamentType) {
    return tournamentType.toLowerCase()
      .replace(/_(.)/g, function ($1) {
        return $1.toUpperCase();
      })
      .replace(/_/g, '');
  }
}

export namespace TournamentType {
  export function canHaveDuplicates(type: TournamentType | undefined): boolean {
    return type === TournamentType.LOOP;
  }
}

export namespace TournamentType {
  export function needsDrawResolution(type: TournamentType | undefined): boolean {
    return type === TournamentType.KING_OF_THE_MOUNTAIN;
  }
}
