export enum ImageFormat {
  RAW = 'RAW',
  BASE64 = 'BASE64',
}

export namespace ImageFormat {
  export function getByKey(key: string): ImageFormat | undefined {
    for (const valueKey in ImageFormat) {
      if ((ImageFormat as any)[valueKey] === key) {
        return (ImageFormat as any)[valueKey];
      }
    }
    return undefined;
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
