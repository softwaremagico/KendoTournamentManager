import {TournamentType} from "./tournament-type";
import {Element} from "./element";
import {TournamentScore} from "./tournament-score.model";

export class Tournament extends Element {
  public static readonly DEFAULT_DUELS_DURATION: number = 180;
  public static readonly DEFAULT_TYPE: TournamentType = TournamentType.LEAGUE;
  public static readonly DEFAULT_SHIAIJOS: number = 1;
  public static readonly DEFAULT_TEAM_SIZE: number = 3;
  public static readonly SHIAIJO_NAMES: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

  public name: string;
  public shiaijos?: number;
  public teamSize: number = 3;
  public type?: TournamentType;
  public duelsDuration: number;
  public tournamentScore: TournamentScore;
  public locked: boolean;
  public lockedAt: Date | undefined;
  private startedAt: Date | undefined;
  public finishedAt: Date | undefined;

  public get scoreRules(): string {
    return this.tournamentScore ? this.tournamentScore.scoreType.toLowerCase() + 'Hint' + 'Hint' : "";
  }

  constructor() {
    super();
    this.tournamentScore = new TournamentScore();
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
    target.lockedAt = source.lockedAt;
    target.startedAt = source.startedAt;
    target.finishedAt = source.finishedAt;
  }

  public static clone(data: Tournament): Tournament {
    const instance: Tournament = new Tournament();
    this.copy(data, instance);
    return instance;
  }
}
