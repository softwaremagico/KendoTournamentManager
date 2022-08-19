import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../models/tournament";
import {Team} from "../../../models/team";
import {CdkDrag, CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
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
    this.fight.duels.push(duel);
  }

  ngOnInit(): void {
  }

  closeDialog() {
    this.dialogRef.close();
  }

  checkDroppedElementTeam1(item: CdkDrag<Participant>) {
    return this.checkDroppedElement(item, this.fight.team1);
  };

  checkDroppedElementTeam2(item: CdkDrag<Participant>) {
    return this.checkDroppedElement(item, this.fight.team2);
  };

  checkDroppedElement(item: CdkDrag<Participant>, team: Team) {
    if (!team.members.includes(item.data)) {
      return false;
    }
    return this.fight.duels[0].competitor1 === undefined;
  }

  transferCard(event: CdkDragDrop<Participant[], any>): Participant {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    return event.container.data[event.currentIndex];
  }

  dropParticipant(event: CdkDragDrop<Participant[], any>, team: Team) {
    const participant: Participant = this.transferCard(event);
    if (this.fight.team1 === team) {
      this.fight.duels[0].competitor1 = participant;
    } else {
      this.fight.duels[0].competitor2 = participant;
    }
  }
}
