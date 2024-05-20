import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {RbacService} from "../../services/rbac/rbac.service";
import {TranslateService} from "@ngx-translate/core";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Duel} from "../../models/duel";
import {Fight} from "../../models/fight";
import {Subject} from "rxjs";
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

  private readonly participantId: number | undefined;

  filteredFights: Map<Tournament, Fight[]>;
  filteredUnties: Map<Tournament, Duel[]>;
  tournaments: Tournament[];

  competitorFights: Map<Tournament, Fight[]>;
  competitorUndraws: Map<Tournament, Duel[]>;

  resetFilterValue: Subject<boolean> = new Subject();

  constructor(private router: Router, private activatedRoute: ActivatedRoute,
              private systemOverloadService: SystemOverloadService, private fightService: FightService,
              rbacService: RbacService, private translateService: TranslateService,
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
        this.goBackToUsers();
      }
    }
  }

  goBackToUsers(): void {
    this.router.navigate(['/participants'], {});
  }

  ngOnInit(): void {
    if (this.participantId) {
      this.initializeData();
    } else {
      this.goBackToUsers();
    }
  }

  initializeData(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    if (this.participantId) {
      this.fightService.getFromParticipant(this.participantId).subscribe((_fights: Fight[]): void => {
        //Classify by tournament.
        this.competitorFights = new Map<Tournament, Fight[]>();
        for (let _fight of _fights) {
          if (this.competitorFights.get(_fight.tournament) == undefined) {
            this.competitorFights.set(_fight.tournament, []);
          }
          this.competitorFights.get(_fight.tournament)?.push(_fight);
        }
        this.tournaments = [...this.competitorFights.keys()];
      });

      this.duelService.getUntiesFromParticipant(this.participantId).subscribe((_undraws: Duel[]): void => {
        //Classify by tournament.
        this.competitorUndraws = new Map<Tournament, Duel[]>();
        for (let _undraw of _undraws) {
          if (this.competitorUndraws.get(_undraw.tournament) == undefined) {
            this.competitorUndraws.set(_undraw.tournament, []);
          }
          this.competitorUndraws.get(_undraw.tournament)?.push(_undraw);
        }
      });
    }
  }


  filter(filter: string): void {
    filter = filter.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "");
    this.filteredFights = new Map<Tournament, Fight[]>();
    this.filteredUnties = new Map<Tournament, Duel[]>();

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


      this.filteredUnties.set(tournament, this.filteredUnties.get(tournament)!.filter((duel: Duel) =>
        (duel.competitor1 ? duel.competitor1!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
        (duel.competitor1 ? duel.competitor1!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor1!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
        (duel.competitor1 && duel.competitor1!.club ? duel.competitor1!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||

        (duel.competitor2 ? duel.competitor2!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
        (duel.competitor2 ? duel.competitor2!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor2!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
        (duel.competitor2 && duel.competitor2!.club ? duel.competitor2!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "")));

    }
  }

  resetFilter(): void {
    this.filter('');
    this.resetFilterValue.next(true);
  }
}
