import {ImageFormat} from "./image-format";
import {Participant} from "./participant";
import {Element} from "./element";

export class ParticipantImage extends Element {

  participant: Participant;
  data: string;
  base64: string;
  imageFormat: ImageFormat;

  public static override copy(source: ParticipantImage, target: ParticipantImage): void {
    Element.copy(source, target);
    target.imageFormat = source.imageFormat;
    if (source.participant !== undefined) {
      target.participant = Participant.clone(source.participant);
    }
    target.data = source.data;
    target.imageFormat = source.imageFormat;
  }

  public static clone(data: ParticipantImage): ParticipantImage {
    const instance: ParticipantImage = new ParticipantImage();
    this.copy(data, instance);
    return instance;
  }
}
