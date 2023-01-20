export enum TournamentImageType {
  BANNER = 'BANNER',
  DIPLOMA = 'DIPLOMA',
  ACCREDITATION = 'ACCREDITATION',
}

export namespace TournamentImageType {
  export function getByKey(key: string): TournamentImageType | undefined {
    for (const valueKey in TournamentImageType) {
      if ((TournamentImageType as any)[valueKey] === key) {
        return (TournamentImageType as any)[valueKey];
      }
    }
    return undefined;
  }
}

export namespace TournamentImageType {
  export function getKeys(): string[] {
    return Object.keys(TournamentImageType).filter(enumValue => (typeof (TournamentImageType[enumValue as TournamentImageType]) !== 'function'))
  }
}

export namespace TournamentImageType {
  export function toArray(): TournamentImageType[] {
    return TournamentImageType.getKeys().map(key => {
      return <TournamentImageType>(<any>TournamentImageType)[key];
    });
  }
}
