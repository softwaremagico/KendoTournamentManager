import {Participant} from "../../models/participant";
import {random} from "../random/random";

export function getBalancedMember(_participants: (Participant | undefined)[], selectFromSector: number, availableSectors: number): Participant {
  const participants: (Participant | undefined)[] = _participants.filter((item: Participant | undefined) => !!item);
  let selected: number = Math.floor(random() * (participants.length / availableSectors));
  let participant: Participant;
  if (selectFromSector == 0) {
    participant = participants[selected]!;
    participants.splice(selected, 1);
  } else if (selectFromSector == availableSectors - 1) {
    selected = participants.length - selected - 1;
    participant = participants[selected]!;
    participants.splice(selected, 1);
  } else {
    selected = Math.floor((participants.length / availableSectors)) * selectFromSector + selected;
    participant = participants[selected]!;
    participants.splice(selected, 1);
  }
  return participant;
}
