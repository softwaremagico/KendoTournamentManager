import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges, ViewEncapsulation} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {Score} from "../../../../../models/score";
import {MessageService} from "../../../../../services/message.service";
import {ScoreUpdatedService} from "../../../../../services/notifications/score-updated.service";
import {TranslocoService} from '@jsverse/transloco';
import {RbacService} from "../../../../../services/rbac/rbac.service";
import {RbacActivity} from "../../../../../services/rbac/rbac.activity";
import {TooltipPositionedComponent} from "../../../../tooltip/tooltip-positioned-component";
import {getTimeRepresentation} from "../../../../../utils/dates/time-representation";
import {takeUntil} from "rxjs";

type ScoreKey = 'competitor1Score' | 'competitor2Score';
type ScoreTimeKey = 'competitor1ScoreTime' | 'competitor2ScoreTime';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'score',
  templateUrl: './score.component.html',
  styleUrls: ['./score.component.scss'],
  // tooltip style not applied without this:
  encapsulation: ViewEncapsulation.None,
})
export class ScoreComponent extends TooltipPositionedComponent implements OnInit, OnChanges {

  @Input()
  index: number;

  @Input()
  duel: Duel;

  @Input()
  left: boolean;

  @Input()
  swapTeams: boolean;

  @Input()
  locked: boolean = true;

  @Input()
  projectMode: boolean;

  @Input()
  over: boolean = false;

  scoreRepresentation: string;
  saving: boolean = false;

  timeRepresentation: string | undefined;

  protected readonly RbacActivity = RbacActivity;
  protected readonly Score = Score;

  constructor(private readonly duelService: DuelService, private readonly scoreUpdatedService: ScoreUpdatedService, private readonly messageService: MessageService,
              private readonly translateService: TranslocoService, public rbacService: RbacService) {
    super();
  }

  ngOnInit(): void {
    this.scoreUpdatedService.isScoreUpdated.pipe(takeUntil(this.destroySubject)).subscribe((duel: Duel): void => {
      if (duel === this.duel) {
        this.scoreRepresentation = this.getScoreRepresentation();
        this.setTime();
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['duel'] || changes['left'] || changes['swapTeams']) {
      this.scoreRepresentation = this.getScoreRepresentation();
      this.setTime();
    }
  }

  private updateDuel(score: Score): boolean {
    if (!score) {
      return false;
    }
    const competitor1Selected: boolean = this.isCompetitor1Selected();
    const scoreKey: ScoreKey = competitor1Selected ? 'competitor1Score' : 'competitor2Score';
    const scoreTimeKey: ScoreTimeKey = competitor1Selected ? 'competitor1ScoreTime' : 'competitor2ScoreTime';
    const updated: boolean = score === Score.EMPTY
      ? this.removeScore(scoreKey, scoreTimeKey)
      : this.setScore(scoreKey, scoreTimeKey, score);
    if (updated) {
      this.scoreUpdatedService.isScoreUpdated.next(this.duel);
    }
    return updated;
  }

  private isCompetitor1Selected(): boolean {
    return (this.left && !this.swapTeams) || (!this.left && this.swapTeams);
  }

  private setScore(scoreKey: ScoreKey, scoreTimeKey: ScoreTimeKey, score: Score): boolean {
    if (this.duel[scoreKey][this.index] === score) {
      return false;
    }
    this.duel[scoreKey][this.index] = score;
    this.duel[scoreTimeKey][this.index] = this.duel.duration!;
    return true;
  }

  private removeScore(scoreKey: ScoreKey, scoreTimeKey: ScoreTimeKey): boolean {
    if (this.duel[scoreKey][this.index] === undefined) {
      return false;
    }
    Reflect.deleteProperty(this.duel[scoreKey], this.index);
    Reflect.deleteProperty(this.duel[scoreTimeKey], this.index);
    return true;
  }

  updateScore(score: Score): void {
    if (this.updateDuel(score)) {
      this.saving = true;
      this.duel.finishedAt = undefined;
      this.duelService.update(this.duel).pipe(takeUntil(this.destroySubject)).subscribe((duel: Duel): Duel => {
        this.messageService.infoMessage('infoScoreUpdated');
        return duel;
      }).add(() => this.saving = false);
    }
  }

  getScoreRepresentation(): string {
    return Score.tag(this.getScore());
  }

  getScore(): Score {
    return this.isCompetitor1Selected()
      ? this.duel.competitor1Score[this.index]
      : this.duel.competitor2Score[this.index];
  }

  possibleScores(): Score[] {
    if (this.locked) {
      return [];
    }
    const competitor1Selected: boolean = this.isCompetitor1Selected();
    if (competitor1Selected ? !this.duel.competitor1 : !this.duel.competitor2) {
      return Score.clear();
    }
    if (competitor1Selected ? !this.duel.competitor2 : !this.duel.competitor1) {
      return Score.noCompetitor();
    }
    return Score.toArray();
  }

  setTime(): void {
    const seconds: number | undefined = this.isCompetitor1Selected() ?
      this.duel.competitor1ScoreTime[this.index] : this.duel.competitor2ScoreTime[this.index];
    this.timeRepresentation = getTimeRepresentation(seconds, (key: string): string => this.translateService.translate(key));
  }

  tooltipText(): string {
    if (!this.timeRepresentation?.length) {
      return "";
    }
    return '<span class="tooltip-score"><b>' + this.getScore() + '</b></span><br>' +
      '<div class="time-tooltip-container"><img src="assets/icons/timer.svg" alt="My Icon" width="16px" height="16px">\n<span class="time-tooltip-text">' + this.timeRepresentation + '</span></div>';
  }

}
