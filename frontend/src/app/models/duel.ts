import {Element} from "./element";
import {Participant} from "./participant";
import {Score} from "./score";
import {DuelType} from "./duel-type";
import {Tournament} from "./tournament";

/**
 * Client-side representation of a single match between two individual competitors
 * within a {@link Fight}.
 *
 * A duel runs for at most {@code tournament.duelsDuration} seconds. The first
 * competitor to score {@code POINTS_TO_WIN} ippon-equivalent points wins the duel.
 * Each scored point is recorded in an ordered {@link Score} list; the parallel
 * {@code *ScoreTime} list captures the time (in seconds) at which each score was made.
 *
 * A duel of type {@link DuelType#UNDECIDED} is an untie duel used to break a draw.
 */
export class Duel extends Element {
  /** The first (left / red) competitor. */
  public competitor1?: Participant;
  /** The second (right / white) competitor. */
  public competitor2?: Participant;
  /** Whether competitor 1 has been awarded a hansoku (serious penalty). */
  public competitor1Fault: boolean;
  /** Whether competitor 2 has been awarded a hansoku (serious penalty). */
  public competitor2Fault: boolean;
  /**
   * Ordered list of ippon scores for competitor 1.
   * Each element is a {@link Score} value (M=Men, K=Kote, T=Do, D=Tsuki, H=Hansoku, I=Invalid).
   */
  public competitor1Score: Score[];
  /**
   * Ordered list of ippon scores for competitor 2.
   * @see competitor1Score
   */
  public competitor2Score: Score[];
  /**
   * Time (in seconds from duel start) of each score in {@link competitor1Score}.
   * Parallel list — index i corresponds to competitor1Score[i].
   */
  public competitor1ScoreTime: number[];
  /**
   * Time (in seconds from duel start) of each score in {@link competitor2Score}.
   * @see competitor1ScoreTime
   */
  public competitor2ScoreTime: number[];
  /** Time (in seconds) at which competitor 1 received a fault, or {@code undefined} if none. */
  public competitor1FaultTime: number | undefined;
  /** Time (in seconds) at which competitor 2 received a fault, or {@code undefined} if none. */
  public competitor2FaultTime: number | undefined;
  /** The type of this duel (regular, untie, etc.). */
  public type: DuelType;
  /** Configured maximum duration of this duel in seconds. */
  public duration?: number;
  /** Total elapsed time (in seconds) from start to finish, including overtime. */
  public totalDuration?: number;
  /** Whether the duel has been finished (either by score or timeout). */
  public finished: boolean;
  /** Timestamp at which the duel started. */
  public startedAt: Date | undefined;
  /** Timestamp at which the duel was concluded. */
  public finishedAt: Date | undefined;
  /** The tournament this duel belongs to. */
  public tournament: Tournament;
  /** Whether this duel involves a substitute member (no ranking impact). */
  public substitute: boolean = false;

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
    target.substitute = source.substitute;
  }

  public static clone(data: Duel): Duel {
    const instance: Duel = new Duel();
    this.copy(data, instance);
    return instance;
  }

  public static addHansoku(duel: Duel, competitor1: boolean): boolean {
    if (competitor1) {
      if (duel.competitor1Score[1] == undefined) {
        duel.competitor1Score[1] = Score.HANSOKU;
        duel.competitor1ScoreTime[1] = duel.duration!;
      } else if (duel.competitor1Score[0] == undefined) {
        duel.competitor1Score[0] = Score.HANSOKU;
        duel.competitor1ScoreTime[0] = duel.duration!;
      } else {
        return false;
      }
    } else if (duel.competitor2Score[1] == undefined) {
      duel.competitor2Score[1] = Score.HANSOKU;
      duel.competitor2ScoreTime[1] = duel.duration!;
    } else if (duel.competitor2Score[0] == undefined) {
      duel.competitor2Score[0] = Score.HANSOKU;
      duel.competitor2ScoreTime[0] = duel.duration!;
    } else {
      return false;
    }
    return true;
  }

  public isStarted(): boolean {
    return this.duration != undefined
      || this.competitor1Score.length > 0 || this.competitor2Score.length > 0
      || this.competitor1Fault || this.competitor2Fault;
  }

  public override toString = (): string => {
    return `Duel{${this.competitor1} vs ${this.competitor2}}`;
  }
}
