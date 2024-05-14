import {Element} from "./element";
import {Participant} from "./participant";
import {Score} from "./score";
import {DuelType} from "./duel-type";
import {Tournament} from "./tournament";

export class Duel extends Element {
  public competitor1?: Participant;
  public competitor2?: Participant;
  public competitor1Fault: boolean;
  public competitor2Fault: boolean;
  public competitor1Score: Score[];
  public competitor2Score: Score[];
  public competitor1ScoreTime: number[];
  public competitor2ScoreTime: number[];
  public competitor1FaultTime: number | undefined;
  public competitor2FaultTime: number | undefined;
  public type: DuelType;
  public duration?: number;
  public totalDuration?: number;
  public finished: boolean;
  public startedAt: Date | undefined;
  public finishedAt: Date | undefined;
  public tournament: Tournament;

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
    source.competitor1Score.forEach((score: Score) => target.competitor1Score.push(score));
    source.competitor2Score.forEach((score: Score) => target.competitor2Score.push(score));
    source.competitor1ScoreTime.forEach((time: number) => target.competitor1ScoreTime.push(time));
    source.competitor2ScoreTime.forEach((time: number) => target.competitor2ScoreTime.push(time));

    target.competitor1FaultTime = source.competitor1FaultTime;
    target.competitor2FaultTime = source.competitor2FaultTime;
    target.finished = source.finished;
    target.startedAt = source.startedAt;
    target.finishedAt = source.finishedAt;
    if (source.tournament) {
      target.tournament = Tournament.clone(source.tournament);
    }
  }

  public static clone(data: Duel): Duel {
    const instance: Duel = new Duel();
    this.copy(data, instance);
    return instance;
  }
}
