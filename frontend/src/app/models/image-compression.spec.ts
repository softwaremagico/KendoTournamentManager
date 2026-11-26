import {ImageCompression} from './image-compression';

describe('ImageCompression', () => {
  it('resolves compression by key and MIME type', () => {
    expect(ImageCompression.getByKey('PNG')).toBe(ImageCompression.PNG);
    expect(ImageCompression.getByKey('UNKNOWN')).toBeUndefined();
    expect(ImageCompression.getByType('image/png')).toBe(ImageCompression.PNG);
    expect(ImageCompression.getByType('image/jpeg')).toBe(ImageCompression.JPG);
    expect(ImageCompression.getByType('image/gif')).toBeUndefined();
  });

  it('exposes all supported values', () => {
    expect(ImageCompression.getKeys()).toEqual(['PNG', 'JPG']);
    expect(ImageCompression.toArray()).toEqual([ImageCompression.PNG, ImageCompression.JPG]);
  });
});
