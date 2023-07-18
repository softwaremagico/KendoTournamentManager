import {Component, ElementRef, Input, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Group} from "../../models/group";

@Component({
  selector: 'app-tournament-brackets',
  templateUrl: './tournament-brackets.component.html',
  styleUrls: ['./tournament-brackets.component.scss']
})
export class TournamentBracketsComponent implements OnInit {

  @Input()
  groups: Group[];

  // @ViewChildren('group', {read: ElementRef})
  // public dynComponents: QueryList<ElementRef>;

  @ViewChild('group-0') group0: ElementRef;
  @ViewChild('group-4') group4: ElementRef;
  @ViewChild('group-7') group7: ElementRef;


  groupsByLevel: Map<number, Group[]>;


  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['groups']) {
      this.groupsByLevel = this.convert(this.groups);
    }
  }

  private convert(groups: Group[]): Map<number, Group[]> {
    const groupsByLevel: Map<number, Group[]> = new Map();
    for (const group of groups) {
      if (group.level !== undefined) {
        if (!groupsByLevel.get(group.level)) {
          groupsByLevel.set(group.level, []);
        }
        groupsByLevel.get(group.level)?.push(group);
      }
    }
    console.log(groupsByLevel)
    return groupsByLevel;
  }

}
