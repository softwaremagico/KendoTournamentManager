import {TournamentType} from "./tournament-type";
import {Element} from "./element";
import {TournamentScore} from "./tournament-score.model";

/**
 * Client-side representation of a Kendo Tournament Manager tournament.
 *
 * Mirrors the backend {@code Tournament} JPA entity and is used throughout
 * the Angular application as the primary data transfer object for tournament
 * configuration.
 */
export class Tournament extends Element {
  /** Default individual duel duration in seconds (3 minutes). */
  public static readonly DEFAULT_DUELS_DURATION: number = 180;
  /** Default tournament format applied when creating a new tournament. */
  public static readonly DEFAULT_TYPE: TournamentType = TournamentType.LEAGUE;
  /** Default number of simultaneous fighting areas (shiaijos). */
  public static readonly DEFAULT_SHIAIJOS: number = 1;
  /** Default number of members per team. */
  public static readonly DEFAULT_TEAM_SIZE: number = 3;
  /** Labels for up to 26 shiaijos, named A–Z. */
  public static readonly SHIAIJO_NAMES: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

  /** Human-readable name that uniquely identifies the tournament. */
  public name: string;
  /** Number of simultaneous shiaijos (fighting areas). */
  public shiaijos?: number;
  /** Maximum number of members per team (including substitutes). */
  public teamSize: number = 3;
  /** Number of members per team that actively compete in each fight. */
  public fightSize: number = 3;
  /** Structural format of the tournament (league, championship, etc.). */
  public type?: TournamentType;
  /** Maximum duration of each duel in seconds. */
  public duelsDuration: number;
  /** Scoring rules that determine how competitors and teams are ranked. */
  public tournamentScore: TournamentScore;
  /** Whether the tournament has been locked (no further score edits allowed). */
  public locked: boolean;
  /** Timestamp at which the tournament was locked, or {@code undefined} if still open. */
  public lockedAt: Date | undefined;
  private startedAt: Date | undefined;
  /** Timestamp at which the last fight was finished and the tournament concluded. */
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
    target.fightSize = source.fightSize;
    target.type = source.type;
    target.duelsDuration = source.duelsDuration;
    target.tournamentScore = TournamentScore.clone(source.tournamentScore);
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
