import {DrawResolution} from "./draw-resolution";

export enum TournamentExtraPropertyKey {
  MAXIMIZE_FIGHTS = 'MAXIMIZE_FIGHTS',
  AVOID_DUPLICATES = 'AVOID_DUPLICATES',
  KING_DRAW_RESOLUTION = 'KING_DRAW_RESOLUTION',
  DIPLOMA_NAME_HEIGHT = 'DIPLOMA_NAME_HEIGHT',
  NUMBER_OF_WINNERS = 'NUMBER_OF_WINNERS',
  LEAGUE_FIGHTS_ORDER_GENERATION = 'LEAGUE_FIGHTS_ORDER_GENERATION'
}

export namespace TournamentExtraPropertyKey {

  export function getDefaultMaximizedFights(): boolean {
    return false;
  }

  export function getDefaultKingDrawResolutions(): DrawResolution {
    return DrawResolution.BOTH_ELIMINATED;
  }

  export function getDefaultLeagueFightsOrderGeneration(): boolean {
    return true;
  }

  export function avoidDuplicateFightsGeneration(): boolean {
    return true;
  }

  export function getByKey(key: string) {
    for (const valueKey in TournamentExtraPropertyKey) {
      if ((TournamentExtraPropertyKey as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }

  export function getKeys(): string[] {
    return Object.keys(TournamentExtraPropertyKey).filter(enumValue => (typeof (TournamentExtraPropertyKey[enumValue as TournamentExtraPropertyKey]) !== 'function'))
  }

  export function toArray(): TournamentExtraPropertyKey[] {
    return TournamentExtraPropertyKey.getKeys().map(key => {
      return <TournamentExtraPropertyKey>(<any>TournamentExtraPropertyKey)[key];
    });
  }

  export function getEnumKeyByEnumValue<T extends {
    [index: string]: string
  }>(myEnum: T, enumValue: string): keyof T | null {
    let keys = Object.keys(myEnum).filter(x => myEnum[x] == enumValue);
    return keys.length > 0 ? keys[0] : null;
  }
}

