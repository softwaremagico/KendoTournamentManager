import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {Participant} from "../../../../../models/participant";
import {debounceTime, fromEvent, Subscription, takeUntil} from "rxjs";
import {NameUtilsService} from "../../../../../services/name-utils.service";
import {MembersOrderChangedService} from "../../../../../services/notifications/members-order-changed.service";
import {KendoComponent} from "../../../../kendo-component";
import {Duel} from "../../../../../models/duel";
import {Router} from "@angular/router";
import {Fight} from "../../../../../models/fight";

@Component({
  selector: 'user-name',
  templateUrl: './user-name.component.html',
  styleUrls: ['./user-name.component.scss']
})
export class UserNameComponent extends KendoComponent implements OnInit, OnChanges {

  @Input()
  participant: Participant | undefined;

  @Input()
  fight: Fight;

  @Input()
  duel: Duel;

  @Input()
  memberIndex: number;

  @Input()
  left: boolean;

  @Input()
  swapTeams: boolean;

  @Input()
  highlightedParticipantId: number | undefined;

  reorderAllowed: boolean = true;

  resizeSubscription$: Subscription;

  public displayName: string = '';

  constructor(protected router: Router, private nameUtilsService: NameUtilsService, private membersOrderChangedService: MembersOrderChangedService) {
    super();
  }

  ngOnInit(): void {
    this.membersOrderChangedService.membersOrderAllowed.pipe(takeUntil(this.destroySubject)).subscribe(enabled =>
      this.reorderAllowed = enabled && !this.duel.finished && !this.isOver(this.fight));
    this.resizeSubscription$ = fromEvent(window, 'resize').pipe(debounceTime(100))
      .subscribe(() => {
        this.displayName = this.getDisplayName(window.innerWidth);
      });
    this.displayName = this.getDisplayName(window.innerWidth);
  }


  ngOnChanges(): void {
    this.displayName = this.getDisplayName(window.innerWidth);
  }

  getShortName(): string {
    return this.nameUtilsService.getShortName(this.participant);
  }

  getShortLastName(): string {
    return this.nameUtilsService.getShortLastName(this.participant);
  }

  getLastname(): string {
    return this.nameUtilsService.getLastname(this.participant);
  }

  getDisplayName(resolution: number): string {
    return this.nameUtilsService.getDisplayName(this.participant, resolution);
  }

  highlightParticipant(): boolean {
    return (this.participant?.id === this.highlightedParticipantId);
  }

  openStatistics(): void {
    this.router.navigateByUrl("/participants/statistics?participantId=" + this.participant?.id + "&tournamentId=" + this.duel.tournament.id + "&redirectUrl=/tournaments/fights");
  }

  isOver(fight: Fight): boolean {
    for (let duel of fight.duels) {
      if (!duel.finished && !duel.substitute) {
        return false;
      }
    }
    return true;
  }

}
