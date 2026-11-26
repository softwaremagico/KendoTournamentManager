import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges, ViewEncapsulation} from '@angular/core';
import {Duel} from "../../../../../models/duel";
import {DuelService} from "../../../../../services/duel.service";
import {MessageService} from "../../../../../services/message.service";
import {ScoreUpdatedService} from "../../../../../services/notifications/score-updated.service";
import {TranslocoService} from '@jsverse/transloco';
import {RbacService} from "../../../../../services/rbac/rbac.service";
import {RbacActivity} from "../../../../../services/rbac/rbac.activity";

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'fault',
  templateUrl: './fault.component.html',
  styleUrls: ['./fault.component.scss'],
  // tooltip style not applied without this:
  encapsulation: ViewEncapsulation.None,
})
export class FaultComponent implements OnInit, OnChanges {

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

  mouseX: number | undefined;
  mouseY: number | undefined;
  screenHeight: number | undefined;
  screenWidth: number | undefined;
  onLeftBorder: boolean;
  onRightBorder: boolean;

  constructor(private duelService: DuelService, private scoreUpdatedService: ScoreUpdatedService, private messageService: MessageService,
              private translateService: TranslocoService, public rbacService: RbacService) {
  }

  ngOnInit(): void {
    // This is intentional
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
    let seconds: number | undefined = (this.left && !this.swapTeams) || (!this.left && this.swapTeams) ?
      this.duel.competitor1FaultTime : this.duel.competitor2FaultTime;
    if (seconds) {
      const minutes: number | undefined = seconds ? Math.floor(seconds / 60) : undefined;
      seconds = seconds % 60;
      let text: string = "";
      if (minutes) {
        text += minutes + " " + this.translateService.translate('minutesAbbreviation') + " ";
      }
      if (seconds) {
        text += seconds + " " + this.translateService.translate('secondsAbbreviation') + " ";
      }
      this.timeRepresentation = text;
    } else {
      this.timeRepresentation = undefined;
    }
  }

  tooltipText(): string {
    if (!this.timeRepresentation || this.timeRepresentation.length == 0) {
      return "";
    }
    let tooltipText: string = '<b>' + this.translateService.translate('fault') + '</b><br>' +
      '<div class="time-tooltip-container"><span class="material-icons time-tooltip">timer</span><span class="time-tooltip">' + this.timeRepresentation + '</span></div>';
    return tooltipText;
  }

  updateCoordinates($event: MouseEvent): void {
    this.mouseX = $event.clientX;
    this.mouseY = $event.clientY;
    this.calculateTooltipMargin();
  }

  clearCoordinates($event: MouseEvent): void {
    this.mouseX = undefined;
    this.mouseY = undefined;
  }


  calculateTooltipMargin(): void {
    this.screenHeight = window.innerHeight;
    this.screenWidth = window.innerWidth;
    this.onLeftBorder = false;
    this.onRightBorder = false;
    if (this.mouseX! - 150 < 0) {
      this.onLeftBorder = true;
    }
    if (this.mouseX! + 150 > this.screenWidth) {
      this.onRightBorder = true;
    }
  }

  protected readonly RbacActivity = RbacActivity;
}
