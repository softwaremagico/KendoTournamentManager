import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Fight} from "../../models/fight";
import {Duel} from "../../models/duel";
import {DuelChangedService} from "../../services/notifications/duel-changed.service";
import {takeUntil} from "rxjs";
import {RbacBasedComponent} from "../RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {TournamentType} from "../../models/tournament-type";
import {MembersOrderChangedService} from "../../services/notifications/members-order-changed.service";

@Component({
  selector: 'fight',
  templateUrl: './fight.component.html',
  styleUrls: ['./fight.component.scss']
})
export class FightComponent extends RbacBasedComponent implements OnInit {

  readonly ROW_HIGH: number = 70;

  protected readonly TournamentType = TournamentType;

  @Input()
  fight: Fight;

  @Input()
  selected: boolean;

  @Input()
  over: boolean;

  @Input()
  locked: boolean;

  @Input()
  projectMode: boolean;

  @Output()
  onSelectedDuel: EventEmitter<Duel> = new EventEmitter();

  selectedDuel: Duel | undefined;

  @Input()
  swapColors: boolean;

  @Input()
  highlightedParticipantId: number | undefined;

  @Input()
  swapTeams: boolean;

  @Input()
  showAvatars: boolean = false;

  @Input()
  onlyShow: boolean = false;

  reorderAllowed: boolean = true;

  constructor(private duelChangedService: DuelChangedService, rbacService: RbacService, private membersOrderChangedService: MembersOrderChangedService) {
    super(rbacService);
    this.membersOrderChangedService.membersOrderAllowed.pipe(takeUntil(this.destroySubject)).subscribe(enabled => this.reorderAllowed = enabled);
  }

  ngOnInit(): void {
    this.duelChangedService.isDuelUpdated.pipe(takeUntil(this.destroySubject)).subscribe(selectedDuel => {
      if (selectedDuel && this.fight?.duels) {
        this.selected = false;
        this.selectedDuel = undefined;
        for (let duel of this.fight.duels) {
          if (selectedDuel === duel) {
            this.selected = true;
            this.selectedDuel = selectedDuel;
          }
        }
      }
    });
  }

  showTeamTitle(): boolean {
    if (this.fight?.tournament?.teamSize) {
      return this.fight.tournament.teamSize > 1;
    }
    return true;
  }

  selectDuel(duel: Duel): void {
    if (this.rbacService.isAllowed(RbacActivity.SELECT_DUEL) && !duel.substitute) {
      this.selectedDuel = duel;
      this.onSelectedDuel.emit(duel);
    }
  }

  isOver(duel: Duel): boolean {
    return duel.substitute || (duel.finished && !this.locked);
  }
}
