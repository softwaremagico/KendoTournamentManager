import {Component, OnInit} from '@angular/core';
import {Group} from "../../../models/group";
import {Router} from "@angular/router";

@Component({
  selector: 'app-tournament-generator',
  templateUrl: './tournament-generator.component.html',
  styleUrls: ['./tournament-generator.component.scss']
})
export class TournamentGeneratorComponent implements OnInit {

  tournamentId: number;

  groups: Group[];

  relations: Map<number, { src: number, dest: number }[]>;


  constructor(private router: Router) {
    const state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      if (state['tournamentId'] && !isNaN(Number(state['tournamentId']))) {
        this.tournamentId = Number(state['tournamentId']);
      } else {
        this.goBackToTournament();
      }
    } else {
      this.goBackToTournament();
    }
  }

  ngOnInit(): void {
  }

  goBackToTournament(): void {
    this.router.navigate(['/tournaments'], {});
  }

}
