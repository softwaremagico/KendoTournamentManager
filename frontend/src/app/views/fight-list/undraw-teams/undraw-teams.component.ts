import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../models/tournament";
import {Team} from "../../../models/team";
import {Participant} from "../../../models/participant";
import {Duel} from "../../../models/duel";
import {DuelType} from "../../../models/duel-type";
import {UntieAddedService} from "../../../services/untie-added.service";
import {GroupService} from "../../../services/group.service";
import {MessageService} from "../../../services/message.service";

@Component({
  selector: 'app-undraw-teams',
  templateUrl: './undraw-teams.component.html',
  styleUrls: ['./undraw-teams.component.scss']
})
export class UndrawTeamsComponent implements OnInit {

  duel: Duel;
  team1: Team;
  team2: Team;
  tournament: Tournament;
  groupId: number;

  constructor(public dialogRef: MatDialogRef<UndrawTeamsComponent>, private untieAddedService: UntieAddedService,
              @Optional() @Inject(MAT_DIALOG_DATA) private data: { tournament: Tournament, groupId: number, team1: Team, team2: Team },
              private groupServices: GroupService, private messageService: MessageService, public dialog: MatDialog) {
    this.team1 = data.team1;
    this.team2 = data.team2;
    this.groupId = data.groupId;
    this.tournament = data.tournament;
    this.duel = new Duel();
    this.duel.totalDuration = data.tournament.duelsDuration;
    this.duel.type = DuelType.UNDRAW;
  }

  ngOnInit(): void {
  }

  createFight() {
    this.groupServices.addUntie(this.groupId, this.duel).subscribe(() => {
      this.messageService.infoMessage("addFight");
      this.untieAddedService.isDuelAdded.next(this.duel);
      this.dialogRef.close();
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  setCompetitor1(participant: Participant) {
    this.duel.competitor1 = participant;
  }

  setCompetitor2(participant: Participant) {
    this.duel.competitor2 = participant;
  }
}
