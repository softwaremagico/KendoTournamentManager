import {Component, OnInit} from '@angular/core';
import {Group} from "../../../models/group";
import {Router} from "@angular/router";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {Tournament} from "../../../models/tournament";
import {TournamentService} from "../../../services/tournament.service";

@Component({
  selector: 'app-tournament-generator',
  templateUrl: './tournament-generator.component.html',
  styleUrls: ['./tournament-generator.component.scss']
})
export class TournamentGeneratorComponent extends RbacBasedComponent implements OnInit {

  tournamentId: number;
  tournament: Tournament;

  groups: Group[];

  relations: Map<number, { src: number, dest: number }[]>;


  constructor(private router: Router, rbacService: RbacService, private tournamentService: TournamentService) {
    super(rbacService);
    const state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      if (state['tournamentId'] && !isNaN(Number(state['tournamentId']))) {
        this.tournamentId = Number(state['tournamentId']);
      } else {
        this.goBackToFights();
      }
    } else {
      this.goBackToFights();
    }
  }

  ngOnInit(): void {
    this.tournamentService.get(this.tournamentId).subscribe((tournament: Tournament): void => {
      this.tournament = tournament;
    });
  }

  goBackToFights(): void {
    this.router.navigate(['/tournaments/fights'], {state: {tournamentId: this.tournamentId}});
  }

  addGroup(): void {

  }

  deleteGroup(): void {

  }
}
