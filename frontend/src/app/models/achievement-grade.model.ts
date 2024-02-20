export enum AchievementGrade {

  NORMAL = 'NORMAL',
  BRONZE = 'BRONZE',
  SILVER = 'SILVER',
  GOLD = 'GOLD'
}

export namespace AchievementGrade {
  export function getByKey(key: string) {
    for (const valueKey in AchievementGrade) {
      if ((AchievementGrade as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return undefined;
  }
}

export namespace AchievementGrade {
  export function getKeys(): string[] {
    return Object.keys(AchievementGrade).filter(enumValue => (typeof (AchievementGrade[enumValue as AchievementGrade]) !== 'function'))
  }
}

export namespace AchievementGrade {
  export function toArray(): AchievementGrade[] {
    return AchievementGrade.getKeys().map(key => {
      return <AchievementGrade>(<any>AchievementGrade)[key];
    });
  }
}
