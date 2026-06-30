export enum SwissTieBreakRule {
  BUCHHOLZ = 'BUCHHOLZ',
  MEDIAN_BUCHHOLZ = 'MEDIAN_BUCHHOLZ',
  SONNEBORN_BERGER = 'SONNEBORN_BERGER',
  DIRECT_ENCOUNTER = 'DIRECT_ENCOUNTER',
  POINT_DIFFERENTIAL = 'POINT_DIFFERENTIAL'
}

export namespace SwissTieBreakRule {
  export function getByKey(name: string): SwissTieBreakRule | undefined {
    for (const key in SwissTieBreakRule) {
      if ((SwissTieBreakRule as any)[key] === name) {
        return (SwissTieBreakRule as any)[key];
      }
    }
    return undefined;
  }

  export function toArray(): SwissTieBreakRule[] {
    return Object.keys(SwissTieBreakRule)
      .filter(k => typeof (SwissTieBreakRule as any)[k] !== 'function')
      .map(k => (SwissTieBreakRule as any)[k]);
  }

  export function toCamel(rule: SwissTieBreakRule): string {
    return rule.toLowerCase()
      .replaceAll(/_(.)/g, (_, c) => c.toUpperCase());
  }
}


