import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../models/duel";
import {DuelChangedService} from "../../../services/notifications/duel-changed.service";
import {CdkDrag, CdkDragDrop, CdkDropList} from "@angular/cdk/drag-drop";
import {Participant} from "../../../models/participant";
import {Fight} from "../../../models/fight";
import {MembersOrderChangedService} from "../../../services/notifications/members-order-changed.service";
import {takeUntil} from "rxjs";
import {KendoComponent} from "../../kendo-component";

@Component({
  selector: 'duel',
  templateUrl: './duel.component.html',
  styleUrls: ['./duel.component.scss']
})
export class DuelComponent extends KendoComponent implements OnInit {

  @Input()
  fight: Fight;

  @Input()
  duel: Duel;

  @Input()
  locked: boolean;

  @Input()
  duelIndex: number;

  @Input()
  selected: boolean;

  @Input()
  swapTeams: boolean;

  @Input()
  showAvatars: boolean = false;

  constructor(private duelChangedService: DuelChangedService, private membersOrderChangedService: MembersOrderChangedService) {
    super();
  }

  ngOnInit(): void {
    this.duelChangedService.isDuelUpdated.pipe(takeUntil(this.destroySubject)).subscribe(selectedDuel => {
      if (selectedDuel && this.duel) {
        this.selected = (selectedDuel.id === this.duel.id);
      }
    });
  }

  dropListEnterPredicate(fight: Fight, left: boolean) {
    return function (_item: CdkDrag<Participant | undefined>, dropList: CdkDropList): boolean {
      if (left) {
        return fight.team1.members.filter(m => m?.id == _item.data?.id).length > 0;
      } else {
        return fight.team2.members.filter(m => m?.id == _item.data?.id).length > 0;
      }
    };
  }

  drop(event: CdkDragDrop<Participant | undefined, any>, left: boolean) {
    let previousIndex: number;
    if (left) {
      previousIndex = this.fight.duels.findIndex(d => d.competitor1 == undefined && event.item.data == undefined || d.competitor1?.id == event.item.data?.id);
    } else {
      previousIndex = this.fight.duels.findIndex(d => d.competitor2 == undefined && event.item.data == undefined || d.competitor2?.id == event.item.data?.id);
    }
    this.swapMembers(previousIndex, this.duelIndex, left);
  }

  swapMembers(source: number, destination: number, left: boolean) {
    if (left) {
      const movingMember: Participant | undefined = this.fight.duels[source].competitor1;
      this.fight.duels[source].competitor1 = this.fight.duels[destination].competitor1;
      this.fight.duels[destination].competitor1 = movingMember;
    } else {
      const movingMember: Participant | undefined = this.fight.duels[source].competitor2;
      this.fight.duels[source].competitor2 = this.fight.duels[destination].competitor2;
      this.fight.duels[destination].competitor2 = movingMember;
    }
    this.membersOrderChangedService.membersOrderChanged.next(this.fight);
  }
}
