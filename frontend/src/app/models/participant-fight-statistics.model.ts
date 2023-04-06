import {Element} from "./element";

export class ParticipantFightStatistics extends Element {

  menNumber: number;
  koteNumber: number;
  doNumber: number;
  tsukiNumber: number;
  hansokuNumber: number;
  ipponNumber: number;
  receivedMenNumber: number;
  receivedKoteNumber: number;
  receivedDoNumber: number;
  receivedTsukiNumber: number;
  receivedHansokuNumber: number;
  receivedipponNumber: number;
  duelsNumber: number;
  //In seconds.
  averageTime: number;
  totalDuelsTime: number;
  faults: number;
  receivedFaults: number;

  public static override copy(source: ParticipantFightStatistics, target: ParticipantFightStatistics): void {
    Element.copy(source, target);
    target.menNumber = source.menNumber;
    target.koteNumber = source.koteNumber;
    target.doNumber = source.doNumber;
    target.tsukiNumber = source.tsukiNumber;
    target.hansokuNumber = source.hansokuNumber;
    target.ipponNumber = source.ipponNumber;
    target.receivedMenNumber = source.receivedMenNumber;
    target.receivedKoteNumber = source.receivedKoteNumber;
    target.receivedDoNumber = source.receivedDoNumber;
    target.receivedTsukiNumber = source.receivedTsukiNumber;
    target.receivedHansokuNumber = source.receivedHansokuNumber;
    target.receivedipponNumber = source.receivedipponNumber;
    target.duelsNumber = source.duelsNumber;
    target.averageTime = source.averageTime;
    target.totalDuelsTime = source.totalDuelsTime;
    target.faults = source.faults;
    target.receivedFaults = source.receivedFaults;
  }

  public static clone(data: ParticipantFightStatistics): ParticipantFightStatistics {
    const instance: ParticipantFightStatistics = new ParticipantFightStatistics();
    this.copy(data, instance);
    return instance;
  }

  public duelsDuration(): number | undefined {
    if (this.duelsNumber && this.averageTime) {
      return this.duelsNumber * this.averageTime;
    }
    return undefined;
  }
}
