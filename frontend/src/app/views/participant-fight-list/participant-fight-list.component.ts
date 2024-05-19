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

@Component({
  selector: 'app-participant-fight-list',
  templateUrl: './participant-fight-list.component.html',
  styleUrls: ['./participant-fight-list.component.scss']
})
export class ParticipantFightListComponent extends RbacBasedComponent implements OnInit {

  private readonly participantId: number | undefined;

  filteredFights: Map<number, Fight[]>;
  filteredUnties: Map<number, Duel[]>;

  resetFilterValue: Subject<boolean> = new Subject();

  constructor(private router: Router, private activatedRoute: ActivatedRoute,
              private systemOverloadService: SystemOverloadService, private fightService: FightService,
              rbacService: RbacService, private translateService: TranslateService,) {
    super(rbacService);

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

      });
    }
  }

  filter(filter: string): void {
    filter = filter.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "");
    this.filteredFights = new Map<number, Fight[]>();
    this.filteredUnties = new Map<number, Duel[]>();

    for (const group of this.groups) {
      if (group.fights) {
        this.filteredFights.set(group.id!, group.fights.filter((fight: Fight) =>
          (this.selectedShiaijo < 0 || fight.shiaijo == this.selectedShiaijo) && (
            (fight.team1 ? fight.team1.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : "") ||
            (fight.team2 ? fight.team2.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : "") ||
            (fight.team1 && fight.team1.members ? fight.team1.members.some(user => user !== undefined && (user.lastname.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              user.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              (user.club ? user.club.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : ""))) : "") ||
            (fight.team2 && fight.team2.members ? fight.team2.members.some(user => user !== undefined && (user.lastname.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              user.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              (user.club ? user.club.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : ""))) : "")))
        );
      } else {
        this.filteredFights.set(group.id!, []);
      }

      if (group.unties) {
        this.filteredUnties.set(group.id!, group.unties.filter((duel: Duel) =>
          (this.selectedShiaijo < 0 || group.shiaijo == this.selectedShiaijo) && (
            (duel.competitor1 ? duel.competitor1!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor1 ? duel.competitor1!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor1!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor1 && duel.competitor1!.club ? duel.competitor1!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||

            (duel.competitor2 ? duel.competitor2!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor2 ? duel.competitor2!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor2!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor2 && duel.competitor2!.club ? duel.competitor2!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : ""))));
      } else {
        this.filteredUnties.set(group.id!, []);
      }
    }
  }

  resetFilter(): void {
    this.filter('');
    this.resetFilterValue.next(true);
  }
}
