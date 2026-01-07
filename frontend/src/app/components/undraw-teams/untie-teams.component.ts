import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {Tournament} from "../../models/tournament";
import {Team} from "../../models/team";
import {Participant} from "../../models/participant";
import {Duel} from "../../models/duel";
import {DuelType} from "../../models/duel-type";
import {UntieAddedService} from "../../services/notifications/untie-added.service";
import {GroupService} from "../../services/group.service";
import {MessageService} from "../../services/message.service";
import {RbacBasedComponent} from "../RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";

@Component({
  selector: 'untie-teams',
  templateUrl: './untie-teams.component.html',
  styleUrls: ['./untie-teams.component.scss']
})
export class UntieTeamsComponent extends RbacBasedComponent implements OnChanges, OnInit {

  @Input()
  teams: Team[] = [];
  @Input()
  tournament: Tournament;
  @Input()
  groupId: number | undefined;
  @Output() onClosed: EventEmitter<Duel[]> = new EventEmitter<Duel[]>();

  duels: Duel[];

  totalDuels: number;

  constructor(private untieAddedService: UntieAddedService,
              private groupServices: GroupService, private messageService: MessageService, public dialog: MatDialog,
              rbacService: RbacService) {
    super(rbacService);
    this.totalDuels = this.getTotalDuels();
    this.duels = [];
  }

  ngOnInit() {
    for (let i = 0; i < this.getTotalDuels(); i++) {
      const duel: Duel = new Duel();
      duel.totalDuration = this.tournament.duelsDuration;
      duel.type = DuelType.UNDRAW;
      duel.tournament = this.tournament;
      this.duels[i] = duel;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['teams']) {
      this.totalDuels = this.getTotalDuels();
    }
  }

  getTotalDuels(): number {
    if (this.teams.length == 2) {
      return 1;
    }
    if (this.teams.length > 2) {
      return this.teams.length;
    }
    return 0;
  }

  duelsCompleted(): boolean {
    for (const duel of this.duels) {
      if (!duel.competitor1 || !duel.competitor2) {
        return false;
      }
    }
    return true;
  }

  createFights(): void {
    if (this.groupId) {
      this.groupServices.addUnties(this.groupId, this.duels).subscribe((): void => {
        this.messageService.infoMessage("addFight");
        this.untieAddedService.isDuelsAdded.next(this.duels);
        this.onClosed.emit(this.duels);
      });
    }
  }

  closeDialog(): void {
    this.onClosed.emit([]);
  }

  setCompetitor1(duelIndex: number, participant: Participant[]): void {
    this.duels[duelIndex].competitor1 = participant[0];
  }

  setCompetitor2(duelIndex: number, participant: Participant[]): void {
    this.duels[duelIndex].competitor2 = participant[0];
  }
}
