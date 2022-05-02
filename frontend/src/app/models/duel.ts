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
}
