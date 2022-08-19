import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../models/tournament";
import {Team} from "../../../models/team";
import {Participant} from "../../../models/participant";
import {Fight} from "../../../models/fight";
import {Duel} from "../../../models/duel";
import {DuelType} from "../../../models/duel-type";

@Component({
  selector: 'app-undraw-teams',
  templateUrl: './undraw-teams.component.html',
  styleUrls: ['./undraw-teams.component.scss']
})
export class UndrawTeamsComponent implements OnInit {

  fight: Fight;

  constructor(public dialogRef: MatDialogRef<UndrawTeamsComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament, team1: Team, team2: Team },
              public dialog: MatDialog) {
    this.fight = new Fight();
    this.fight.team1 = data.team1;
    this.fight.team2 = data.team2;
    this.fight.tournament = data.tournament;
    const duel: Duel = new Duel();
    duel.totalDuration = data.tournament.duelsDuration;
    duel.type = DuelType.UNDRAW;
    this.fight.duels = [];
    this.fight.duels.push(duel);
  }

  ngOnInit(): void {
  }

  closeDialog() {
    this.dialogRef.close();
  }

  setCompetitor1(participant: Participant) {
    this.fight.duels[0].competitor1 = participant;
  }

  setCompetitor2(participant: Participant) {
    this.fight.duels[0].competitor2 = participant;
  }
}
