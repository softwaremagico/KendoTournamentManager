import {Participant} from "./participant";
import {ImageModel} from "./image.model";

export class ParticipantImage extends ImageModel {

  participant: Participant;

  public static override copy(source: ParticipantImage, target: ParticipantImage): void {
    ImageModel.copy(source, target);
    if (source.participant !== undefined) {
      target.participant = Participant.clone(source.participant);
    }
  }

  public static override clone(data: ParticipantImage): ParticipantImage {
    const instance: ParticipantImage = new ParticipantImage();
    this.copy(data, instance);
    return instance;
  }
}
