import {Element} from "./element";
import {ImageFormat} from "./image-format";

export class ImageModel extends Element {

  data: string;
  base64: string;
  imageFormat: ImageFormat;

  public static override copy(source: ImageModel, target: ImageModel): void {
    Element.copy(source, target);
    target.data = source.data;
    target.imageFormat = source.imageFormat;
  }

  public static clone(data: ImageModel): ImageModel {
    const instance: ImageModel = new ImageModel();
    this.copy(data, instance);
    return instance;
  }
}
