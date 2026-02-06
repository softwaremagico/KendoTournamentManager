import {ImageCompression} from "./image-compression";
import {Element} from "./element";
import {Tournament} from "./tournament";
import {TournamentImageType} from "./tournament-image-type";

export class TournamentImage extends Element {

  tournament: Tournament;
  data: string;
  base64: string;
  imageType: TournamentImageType;
  imageCompression: ImageCompression;
  defaultImage: boolean = false;

  public static override copy(source: TournamentImage, target: TournamentImage): void {
    Element.copy(source, target);
    if (source.tournament !== undefined) {
      target.tournament = Tournament.clone(source.tournament);
    }
    target.data = source.data;
    target.imageType = source.imageType;
    target.imageCompression = source.imageCompression;
  }

  public static clone(data: TournamentImage): TournamentImage {
    const instance: TournamentImage = new TournamentImage();
    this.copy(data, instance);
    return instance;
  }
}
