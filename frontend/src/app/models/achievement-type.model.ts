export enum AchievementType {

  BILLY_THE_KID = 'BILLY_THE_KID',

  LETHAL_WEAPON = 'LETHAL_WEAPON',

  TERMINATOR = 'TERMINATOR',

  JUGGERNAUT = 'JUGGERNAUT',

  THE_KING = 'THE_KING',

  LOOKS_GOOD_FROM_FAR_AWAY_BUT = 'LOOKS_GOOD_FROM_FAR_AWAY_BUT',

  I_LOVE_THE_FLAGS = 'I_LOVE_THE_FLAGS',

  THE_CASTLE = 'THE_CASTLE',

  ENTRENCHED = 'ENTRENCHED',

  A_LITTLE_OF_EVERYTHING = 'A_LITTLE_OF_EVERYTHING',

  BONE_BREAKER = 'BONE_BREAKER',

  FLEXIBLE_AS_BAMBOO = 'FLEXIBLE_AS_BAMBOO',

  SWEATY_TENUGUI = 'SWEATY_TENUGUI',

  THE_WINNER = 'THE_WINNER',

  THE_WINNER_TEAM = 'THE_WINNER_TEAM',

  WOODCUTTER = 'WOODCUTTER',

  THE_NEVER_ENDING_STORY = 'THE_NEVER_ENDING_STORY',

  LOVE_SHARING = 'LOVE_SHARING',

  MASTER_THE_LOOP = 'MASTER_THE_LOOP',

  TIS_BUT_A_SCRATCH = 'TIS_BUT_A_SCRATCH',

  FIRST_BLOOD = 'FIRST_BLOOD',

  DARUMA = 'DARUMA',

  STORMTROOPER_SYNDROME = 'STORMTROOPER_SYNDROME',

  V_FOR_VENDETTA = 'V_FOR_VENDETTA',

  SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER = 'SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER',

}

export namespace AchievementType {
  export function getByKey(key: string): string | undefined {
    for (const valueKey in AchievementType) {
      if ((AchievementType as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }
}

export namespace AchievementType {
  export function getKeys(): string[] {
    return Object.keys(AchievementType).filter((enumValue: string): boolean => (typeof (AchievementType[enumValue as AchievementType]) !== 'function'))
  }
}

export namespace AchievementType {
  export function toArray(): AchievementType[] {
    return AchievementType.getKeys().map((key: string) => {
      return <AchievementType>(<any>AchievementType)[key];
    });
  }
}

export namespace AchievementType {
  export function toCamel(achievementType: AchievementType) {
    return achievementType.toLowerCase()
      .replace(/_(.)/g, function ($1: string) {
        return $1.toUpperCase();
      })
      .replace(/_/g, '');
  }
}
