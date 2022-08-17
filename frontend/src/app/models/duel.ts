import {Element} from "./element";
import {Participant} from "./participant";
import {TournamentType} from "./tournament-type";
import {Score} from "./score";

export class Duel extends Element {
  public competitor1?: Participant;
  public competitor2?: Participant;
  public competitor1Fault: boolean;
  public competitor2Fault: boolean;
  public competitor1Score: Score[];
  public competitor2Score: Score[];
  public type: TournamentType;
  public duration?: number;
  public totalDuration?: number;

  public static override copy(source: Duel, target: Duel): void {
    Element.copy(source, target);
    target.competitor1Fault = source.competitor1Fault;
    target.competitor2Fault = source.competitor2Fault;
    target.type = source.type;
    if (source.competitor1) {
      target.competitor1 = Participant.clone(source.competitor1);
    }
    if (source.competitor2) {
      target.competitor2 = Participant.clone(source.competitor2);
    }
    if (source.duration) {
      target.duration = source.duration;
    }
    if (source.totalDuration) {
      target.totalDuration = source.totalDuration;
    }
    target.competitor1Score = [];
    target.competitor2Score = [];
    source.competitor1Score.forEach(score => target.competitor1Score.push(score));
    source.competitor2Score.forEach(score => target.competitor2Score.push(score));
  }

  public static clone(data: Duel): Duel {
    const instance: Duel = new Duel();
    this.copy(data, instance);
    return instance;
  }
}
