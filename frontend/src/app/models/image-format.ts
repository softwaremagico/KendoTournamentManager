export enum ImageFormat {
  RAW = 'RAW',
  BASE64 = 'BASE64',
}

export namespace ImageFormat {
  export function getByKey(key: string): string {
    for (const valueKey in ImageFormat) {
      if ((ImageFormat as any)[valueKey] === key) {
        return valueKey;
      }
    }
    return ImageFormat.RAW;
  }
}

export namespace ImageFormat {
  export function getKeys(): string[] {
    return Object.keys(ImageFormat).filter(enumValue => (typeof (ImageFormat[enumValue as ImageFormat]) !== 'function'))
  }
}

export namespace ImageFormat {
  export function toArray(): ImageFormat[] {
    return ImageFormat.getKeys().map(key => {
      return <ImageFormat>(<any>ImageFormat)[key];
    });
  }
}
