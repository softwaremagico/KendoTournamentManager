export enum ImageCompression {
  PNG = 'PNG',
  JPG = 'JPG',
}

export namespace ImageCompression {
  export function getByKey(key: string): ImageCompression | undefined {
    for (const valueKey in ImageCompression) {
      if ((ImageCompression as any)[valueKey] === key) {
        return (ImageCompression as any)[valueKey];
      }
    }
    return undefined;
  }
}

export namespace ImageCompression {
  export function getByType(type: string): ImageCompression | undefined {
    switch (type) {
      case "image/jpeg":
        return ImageCompression.JPG;
      case "image/png":
        return ImageCompression.PNG;
    }
    return undefined;
  }
}

export namespace ImageCompression {
  export function getKeys(): string[] {
    return Object.keys(ImageCompression).filter(enumValue => (typeof (ImageCompression[enumValue as ImageCompression]) !== 'function'))
  }
}

export namespace ImageCompression {
  export function toArray(): ImageCompression[] {
    return ImageCompression.getKeys().map(key => {
      return <ImageCompression>(<any>ImageCompression)[key];
    });
  }
}
