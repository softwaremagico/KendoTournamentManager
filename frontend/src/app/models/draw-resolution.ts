export enum DrawResolution {
  OLDEST_ELIMINATED = 'OLDEST_ELIMINATED',
  BOTH_ELIMINATED = 'BOTH_ELIMINATED',
  NEWEST_ELIMINATED = 'NEWEST_ELIMINATED',
}

export namespace DrawResolution {
  export function getByKey(key: string): DrawResolution {
    for (const valueKey in DrawResolution) {
      if ((DrawResolution as any)[valueKey] === key) {
        return (DrawResolution as any)[valueKey];
      }
    }
    return DrawResolution.BOTH_ELIMINATED;
  }

  export function getKeys(): string[] {
    return Object.keys(DrawResolution).filter(enumValue => (typeof (DrawResolution[enumValue as DrawResolution]) !== 'function'))
  }

  export function toArray(): DrawResolution[] {
    return DrawResolution.getKeys().map(key => {
      return <DrawResolution>(<any>DrawResolution)[key];
    });
  }

  export function toCamel(drawResolution: DrawResolution) {
    return drawResolution.toLowerCase()
      .replace(/_(.)/g, function ($1) {
        return $1.toUpperCase();
      })
      .replace(/_/g, '');
  }
}
