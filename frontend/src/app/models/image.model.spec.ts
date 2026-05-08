import { ImageModel } from './image.model';

describe('ImageModel', () => {
  const buildImage = (): ImageModel => {
    const image = new ImageModel();
    image.id = 1;
    image.data = 'binary-data';
    image.base64 = 'base64-data';
    image.imageFormat = 'PNG' as any;
    image.createdBy = 'tester';
    return image;
  };

  it('should copy scalar fields into target', () => {
    const source = buildImage();
    const target = new ImageModel();

    ImageModel.copy(source, target);

    expect(target.id).toBe(1);
    expect(target.data).toBe('binary-data');
    expect(target.imageFormat).toBe('PNG' as any);
    expect(target.createdBy).toBe('tester');
  });

  it('should clone image model into a new instance', () => {
    const source = buildImage();

    const clone = ImageModel.clone(source);

    expect(clone).not.toBe(source);
    expect(clone.id).toBe(source.id);
    expect(clone.data).toBe(source.data);
    expect(clone.imageFormat).toBe(source.imageFormat);
  });
});

