import {TournamentType} from "./tournament-type";
import {Element} from "./element";
import {TournamentScore} from "./tournament-score.model";

export class Tournament extends Element {
  public static readonly DEFAULT_DUELS_DURATION: number = 180;
  public static readonly DEFAULT_TYPE: TournamentType = TournamentType.LEAGUE;
  public static readonly DEFAULT_SHIAIJOS: number = 1;
  public static readonly DEFAULT_TEAM_SIZE: number = 3;

  public name: string;
  public shiaijos?: number;
  public teamSize: number = 3;
  public type?: TournamentType;
  public duelsDuration: number;
  public tournamentScore: TournamentScore;
  public locked: boolean;

  public get scoreRules(): string {
    return this.tournamentScore ? this.tournamentScore.scoreType.toLowerCase() + 'Hint' + 'Hint' : "";
  }

  constructor() {
    super();
  }

  public static override copy(source: Tournament, target: Tournament): void {
    Element.copy(source, target);
    target.name = source.name;
    target.shiaijos = source.shiaijos;
    target.teamSize = source.teamSize;
    target.type = source.type;
    target.duelsDuration = source.duelsDuration;
    if (source.tournamentScore !== undefined) {
      target.tournamentScore = TournamentScore.clone(source.tournamentScore);
    }
    target.locked = source.locked;
  }

  public static clone(data: Tournament): Tournament {
    const instance: Tournament = new Tournament();
    this.copy(data, instance);
    return instance;
  }
}
