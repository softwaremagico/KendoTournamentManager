import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Duel} from "../../models/duel";
import {Fight} from "../../models/fight";
import {forkJoin, Subject} from "rxjs";
import {FightService} from "../../services/fight.service";
import {Tournament} from "../../models/tournament";
import {Participant} from "../../models/participant";
import {DuelService} from "../../services/duel.service";

@Component({
  selector: 'app-participant-fight-list',
  templateUrl: './participant-fight-list.component.html',
  styleUrls: ['./participant-fight-list.component.scss']
})
export class ParticipantFightListComponent extends RbacBasedComponent implements OnInit {

  readonly participantId: number | undefined;

  filteredFights: Map<Tournament, Fight[]>;
  filteredUnties: Map<Tournament, Duel[]>;
  tournaments: Tournament[];

  competitorFights: Map<Tournament, Fight[]>;
  competitorUndraws: Map<Tournament, Duel[]>;

  resetFilterValue: Subject<boolean> = new Subject();

  constructor(private router: Router, rbacService: RbacService,
              private systemOverloadService: SystemOverloadService, private fightService: FightService,
              private duelService: DuelService) {
    super(rbacService);
    this.filteredFights = new Map<Tournament, Fight[]>();
    this.filteredUnties = new Map<Tournament, Duel[]>();
    this.tournaments = [];

    const state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      //Send by previous view.
      if (state['participantId'] && !isNaN(Number(state['participantId']))) {
        this.participantId = Number(state['participantId']);
      } else {
        this.goBackToStatistics();
      }
    }
  }

  goBackToStatistics(): void {
    this.router.navigate(['/participants/statistics'], {state: {participantId: this.participantId}});
  }

  ngOnInit(): void {
    if (this.participantId) {
      this.initializeData();
    } else {
      this.goBackToStatistics();
    }
  }

  initializeData(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    if (this.participantId) {

      let fightsRequest = this.fightService.getFromParticipant(this.participantId);
      let untiesRequest = this.duelService.getUntiesFromParticipant(this.participantId);

      forkJoin([fightsRequest, untiesRequest]).subscribe(([_fights, _undraws]): void => {
        //Tournaments by fights.
        this.competitorFights = new Map<Tournament, Fight[]>();
        for (let _fight of _fights) {
          let tournament: Tournament | undefined = [...this.competitorFights.keys()]
            .find((t: Tournament): boolean => t.id === _fight.tournament.id);
          if (tournament === undefined) {
            this.competitorFights.set(_fight.tournament, []);
            tournament = _fight.tournament;
          }
          this.competitorFights.get(tournament)?.push(_fight);
        }
        this.tournaments = [...this.competitorFights.keys()];

        this.tournaments.sort((a: Tournament, b: Tournament): number =>
          a.createdAt && b.createdAt ? (new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()) : 0);

        //Undraw fights.
        this.competitorUndraws = new Map<Tournament, Duel[]>();
        for (let _undraw of _undraws) {
          let tournament: Tournament | undefined = [...this.competitorFights.keys()]
            .find((t: Tournament): boolean => t.id === _undraw.tournament.id);
          if (tournament === undefined) {
            this.competitorUndraws.set(_undraw.tournament, []);
            tournament = _undraw.tournament;
          }
          this.competitorUndraws.get(tournament)?.push(_undraw);
        }

        this.systemOverloadService.isTransactionalBusy.next(false);
        this.resetFilter();
      });
    }
  }


  filter(filter: string): void {
    filter = filter.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "");
    this.filteredFights = new Map<Tournament, Fight[]>();
    this.filteredUnties = new Map<Tournament, Duel[]>();

    if (this.competitorFights) {
      for (const tournament of this.competitorFights.keys()) {
        this.filteredFights.set(tournament, this.competitorFights.get(tournament)!.filter((fight: Fight) =>
          (fight.team1 ? fight.team1.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : "") ||
          (fight.team2 ? fight.team2.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : "") ||
          (fight.team1 && fight.team1.members ? fight.team1.members.some((user: Participant | undefined) => user !== undefined && (user.lastname.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
            user.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
            (user.club ? user.club.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : ""))) : "") ||
          (fight.team2 && fight.team2.members ? fight.team2.members.some((user: Participant | undefined) => user !== undefined && (user.lastname.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
            user.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
            (user.club ? user.club.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : ""))) : "")));

        if (this.filteredUnties.get(tournament) != undefined) {
          this.filteredUnties.set(tournament, this.filteredUnties.get(tournament)!.filter((duel: Duel) =>
            (duel.competitor1 ? duel.competitor1!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor1 ? duel.competitor1!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor1!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor1 && duel.competitor1!.club ? duel.competitor1!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||

            (duel.competitor2 ? duel.competitor2!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor2 ? duel.competitor2!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor2!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor2 && duel.competitor2!.club ? duel.competitor2!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "")));
        }
      }
    }
  }

  resetFilter(): void {
    this.filter('');
    this.resetFilterValue.next(true);
  }
}
