import {Component, OnInit} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {MessageService} from "../../services/message.service";
import {FightService} from "../../services/fight.service";
import {Fight} from "../../models/fight";
import {Tournament} from "../../models/tournament";
import {ClubDialogBoxComponent} from "../club-list/club-dialog-box/club-dialog-box.component";
import {Router} from "@angular/router";
import {TournamentService} from "../../services/tournament.service";
import {Action} from "../../action";

@Component({
  selector: 'app-fight-list',
  templateUrl: './fight-list.component.html',
  styleUrls: ['./fight-list.component.scss']
})
export class FightListComponent implements OnInit {

  fights: Fight[];
  selectedFight: Fight | undefined;
  tournament: Tournament;
  private readonly tournamentId: number | undefined;

  constructor(private router: Router, private tournamentService: TournamentService, private fightService: FightService, public dialog: MatDialog, private messageService: MessageService) {
    let state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      if (state['tournamentId'] && !isNaN(Number(state['tournamentId']))) {
        this.tournamentId = Number(state['tournamentId']);
      } else {
        //Return to tournaments for selecting one.
        this.router.navigate(['/tournaments']);
      }
    } else {
      //Return to tournaments for selecting one.
      this.router.navigate(['/tournaments']);
    }
  }

  ngOnInit(): void {
    if (this.tournamentId) {
      this.tournamentService.get(this.tournamentId).subscribe(tournament => {
        this.tournament = tournament;
        this.fightService.getFromTournament(this.tournament).subscribe(fights => {
          this.fights = fights;
        });
      })
    }
  }

  generateElements() {
    // This is intentional
  }

  addElement() {
    this.openDialog('Add a new Fight', Action.Add, new Fight());
  }

  editElement(): void {
    if (this.selectedFight) {
      this.openDialog('Edit fight', Action.Update, this.selectedFight);
    }
  }

  deleteElement(): void {
    if (this.selectedFight) {
      this.openDialog('Delete fight', Action.Delete, this.selectedFight);
    }
  }

  openDialog(title: string, action: Action, fight: Fight) {
    const dialogRef = this.dialog.open(ClubDialogBoxComponent, {
      width: '250px',
      data: {title: title, action: action, entity: fight}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.action == Action.Add) {
        this.addRowData(result.data);
      } else if (result.action == Action.Update) {
        this.updateRowData(result.data);
      } else if (result.action == Action.Delete) {
        this.deleteRowData(result.data);
      }
    });
  }

  addRowData(fight: Fight) {
    this.fightService.add(fight).subscribe(_fight => {
      this.fights.push(_fight)
      this.messageService.infoMessage("Fight Stored");
    });
  }

  updateRowData(fight: Fight) {
    this.fightService.update(fight).subscribe(() => {
        this.messageService.infoMessage("Fight Updated");
      }
    );
  }

  deleteRowData(fight: Fight) {
    this.fightService.delete(fight).subscribe(() => {
        let currentFights: Fight[] = this.fights.filter(existing_fight => existing_fight !== fight);
        this.messageService.infoMessage("Fight Deleted. Current fights: " + currentFights.length);
      }
    );
  }
}
