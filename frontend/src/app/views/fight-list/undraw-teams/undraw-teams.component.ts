import {Component, Inject, OnChanges, Optional, SimpleChanges} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../models/tournament";
import {Team} from "../../../models/team";
import {Participant} from "../../../models/participant";
import {Duel} from "../../../models/duel";
import {DuelType} from "../../../models/duel-type";
import {UntieAddedService} from "../../../services/notifications/untie-added.service";
import {GroupService} from "../../../services/group.service";
import {MessageService} from "../../../services/message.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {Action} from "../../../action";

@Component({
  selector: 'app-undraw-teams',
  templateUrl: './undraw-teams.component.html',
  styleUrls: ['./undraw-teams.component.scss']
})
export class UndrawTeamsComponent extends RbacBasedComponent implements OnChanges {

  duels: Duel[];
  teams: Team[] = [];
  tournament: Tournament;
  groupId: number;
  totalDuels: number;

  constructor(public dialogRef: MatDialogRef<UndrawTeamsComponent>, private untieAddedService: UntieAddedService,
              @Optional() @Inject(MAT_DIALOG_DATA) private data: { tournament: Tournament, groupId: number, teams: Team[] },
              private groupServices: GroupService, private messageService: MessageService, public dialog: MatDialog,
              rbacService: RbacService) {
    super(rbacService);
    this.teams = data.teams;
    this.totalDuels = this.getTotalDuels();
    this.groupId = data.groupId;
    this.tournament = data.tournament;
    this.duels = [];
    for (let i = 0; i < this.getTotalDuels(); i++) {
      const duel: Duel = new Duel();
      duel.totalDuration = data.tournament.duelsDuration;
      duel.type = DuelType.UNDRAW;
      duel.tournament = data.tournament;
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
    this.groupServices.addUnties(this.groupId, this.duels).subscribe((): void => {
      this.messageService.infoMessage("addFight");
      this.untieAddedService.isDuelsAdded.next(this.duels);
      this.dialogRef.close({action: Action.Update, draws: false});
    });
  }

  closeDialog(): void {
    this.dialogRef.close({action: Action.Cancel, draws: true});
  }

  setCompetitor1(duelIndex: number, participant: Participant): void {
    this.duels[duelIndex].competitor1 = participant;
  }

  setCompetitor2(duelIndex: number, participant: Participant): void {
    this.duels[duelIndex].competitor2 = participant;
  }
}
