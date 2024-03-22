import {Element} from "./element";
import {ImageModel} from "./image.model";
import {Tournament} from "./tournament";

export class QrCode extends ImageModel {

  tournament: Tournament;

  public static override copy(source: QrCode, target: QrCode): void {
    Element.copy(source, target);
    if (source.tournament !== undefined) {
      target.tournament = Tournament.clone(source.tournament);
    }
    target.data = source.data;
    target.imageFormat = source.imageFormat;
  }

  public static override clone(data: QrCode): QrCode {
    const instance: QrCode = new QrCode();
    this.copy(data, instance);
    return instance;
  }
}
