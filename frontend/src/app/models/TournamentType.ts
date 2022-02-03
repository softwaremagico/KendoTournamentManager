export enum TournamentType {
  CHAMPIONSHIP = 'CHAMPIONSHIP',
  TREE = 'TREE',
  LEAGUE = 'LEAGUE',
  LOOP = 'LOOP',
  CUSTOM_CHAMPIONSHIP = 'CUSTOM_CHAMPIONSHIP',
  KING_OF_THE_MOUNTAIN = 'KING_OF_THE_MOUNTAIN',
  CUSTOMIZED = 'CUSTOMIZED'
}

export namespace TournamentType {
  export function getByKey(key: string) {
    for (const valueKey in TournamentType) {
      if ((TournamentType as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }
}
