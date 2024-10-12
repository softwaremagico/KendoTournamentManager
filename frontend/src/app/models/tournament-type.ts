export enum TournamentType {
  CHAMPIONSHIP = 'CHAMPIONSHIP',
  LEAGUE = 'LEAGUE',
  LOOP = 'LOOP',
  // CUSTOM_CHAMPIONSHIP = 'CUSTOM_CHAMPIONSHIP',
  KING_OF_THE_MOUNTAIN = 'KING_OF_THE_MOUNTAIN',
  BUBBLE_SORT = 'BUBBLE_SORT',
  CUSTOMIZED = 'CUSTOMIZED',
  SENBATSU = 'SENBATSU'
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

  export function getKeys(): string[] {
    return Object.keys(TournamentType).filter(enumValue => (typeof (TournamentType[enumValue as TournamentType]) !== 'function'))
  }

  export function toArray(): TournamentType[] {
    return TournamentType.getKeys().map(key => {
      return <TournamentType>(<any>TournamentType)[key];
    });
  }

  export function toCamel(tournamentType: TournamentType) {
    return tournamentType.toLowerCase()
      .replace(/_(.)/g, function ($1) {
        return $1.toUpperCase();
      })
      .replace(/_/g, '');
  }

  export function canMaximizeFights(type: TournamentType | undefined): boolean {
    return type === TournamentType.CHAMPIONSHIP;
  }

  export function avoidsDuplicatedFights(type: TournamentType | undefined): boolean {
    return type === TournamentType.LOOP;
  }

  export function needsDrawResolution(type: TournamentType | undefined): boolean {
    return type === TournamentType.KING_OF_THE_MOUNTAIN || type === TournamentType.BUBBLE_SORT;
  }

  export function canSelectChallengeDistance(type: TournamentType | undefined): boolean {
    return type === TournamentType.SENBATSU;
  }

  export function needsFifoWinner(type: TournamentType | undefined): boolean {
    return type === TournamentType.CHAMPIONSHIP || type === TournamentType.LEAGUE;
  }

  export function resolveOddFightsAsap(type: TournamentType | undefined): boolean {
    return type === TournamentType.CHAMPIONSHIP;
  }

  /**
   * Has a default group that is automatically handled by the system.
   */
  export function usesDefaultGroup(type: TournamentType | undefined): boolean {
    return type !== TournamentType.CHAMPIONSHIP;
  }
}


