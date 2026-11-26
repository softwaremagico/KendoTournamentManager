import {ChangeDetectionStrategy, Component, Input, OnChanges, SimpleChanges, ViewEncapsulation} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {MessageService} from "../../../../../services/message.service";
import {ScoreUpdatedService} from "../../../../../services/notifications/score-updated.service";
import {TranslocoService} from '@jsverse/transloco';
import {RbacService} from "../../../../../services/rbac/rbac.service";
import {RbacActivity} from "../../../../../services/rbac/rbac.activity";
import {TooltipPositionedComponent} from "../../../../tooltip/tooltip-positioned-component";
import {getTimeRepresentation} from "../../../../../utils/dates/time-representation";

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'fault',
  templateUrl: './fault.component.html',
  styleUrls: ['./fault.component.scss'],
  // tooltip style not applied without this:
  encapsulation: ViewEncapsulation.None,
})
export class FaultComponent extends TooltipPositionedComponent implements OnChanges {

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

  timeRepresentation: string | undefined;

  constructor(private duelService: DuelService, private scoreUpdatedService: ScoreUpdatedService, private messageService: MessageService,
              private translateService: TranslocoService, public rbacService: RbacService) {
    super();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['duel'] || changes['left'] || changes['swapTeams']) {
      this.setTime();
    }
  }

  private setFault(fault: boolean): boolean {
    const faultAdded: boolean = this.isCompetitor1Selected()
      ? this.setCompetitor1Fault(fault)
      : this.setCompetitor2Fault(fault);
    this.setTime();
    return faultAdded;
  }

  private isCompetitor1Selected(): boolean {
    return (this.left && !this.swapTeams) || (!this.left && this.swapTeams);
  }

  private setCompetitor1Fault(fault: boolean): boolean {
    if (!fault || !this.duel.competitor1Fault) {
      this.duel.competitor1Fault = fault;
      this.duel.competitor1FaultTime = fault ? this.duel.duration! : undefined;
      return true;
    }
    this.duel.competitor1Fault = false;
    this.duel.competitor1FaultTime = undefined;
    if (Duel.addHansoku(this.duel, false)) {
      this.scoreUpdatedService.isScoreUpdated.next(this.duel);
      return true;
    }
    this.messageService.warningMessage("scoreNotAdded");
    return false;
  }

  private setCompetitor2Fault(fault: boolean): boolean {
    if (!fault || !this.duel.competitor2Fault) {
      this.duel.competitor2Fault = fault;
      this.duel.competitor2FaultTime = fault ? this.duel.duration! : undefined;
      return true;
    }
    this.duel.competitor2Fault = false;
    this.duel.competitor2FaultTime = undefined;
    if (Duel.addHansoku(this.duel, true)) {
      this.scoreUpdatedService.isScoreUpdated.next(this.duel);
      return true;
    }
    this.messageService.warningMessage("scoreNotAdded");
    return false;
  }

  updateFault(fault: boolean): void {
    const faultAdded: boolean = this.setFault(fault);
    this.duelService.update(this.duel).subscribe((duel: Duel): Duel => {
      if (faultAdded) {
        this.messageService.infoMessage('infoFaultUpdated');
      }
      return duel;
    });
  }

  setTime(): void {
    const seconds: number | undefined = this.isCompetitor1Selected() ?
      this.duel.competitor1FaultTime : this.duel.competitor2FaultTime;
    this.timeRepresentation = getTimeRepresentation(seconds, (key: string): string => this.translateService.translate(key));
  }

  tooltipText(): string {
    if (!this.timeRepresentation || this.timeRepresentation.length == 0) {
      return "";
    }
    let tooltipText: string = '<b>' + this.translateService.translate('fault') + '</b><br>' +
      '<div class="time-tooltip-container"><span class="material-icons time-tooltip">timer</span><span class="time-tooltip">' + this.timeRepresentation + '</span></div>';
    return tooltipText;
  }

  protected readonly RbacActivity = RbacActivity;
}
