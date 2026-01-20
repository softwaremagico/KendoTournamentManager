export abstract class BracketsMeasures {
  static readonly GROUP_HIGH: number = 100;
  static readonly GROUP_WIDTH: number = 300;
  static readonly GROUP_SEPARATION: number = 150;
  private static readonly LEVEL_SEPARATION: number = 100;
  private static readonly LEVEL_SEPARATION_EXTRA: number = 75;
  static readonly SHIAIJO_PADDING: number = 15;
  static readonly TEAM_GROUP_HIGH: number = 80;
  static readonly WINNER_ARROWS_SEPARATION: number = 10;

  static levelSeparation(groupsAtLevelZero: number | undefined): number {
    if (groupsAtLevelZero) {
      return this.LEVEL_SEPARATION + (groupsAtLevelZero * this.LEVEL_SEPARATION_EXTRA);
    }
    return this.LEVEL_SEPARATION;
  }
}
