import {Component, Input, OnInit} from '@angular/core';
import {TeamListData} from "./team-list-data";

@Component({
  selector: 'team-list',
  templateUrl: './team-list.component.html',
  styleUrls: ['./team-list.component.scss']
})
export class TeamListComponent implements OnInit {

  @Input()
  teamListData: TeamListData;
  filterString: string;

  ngOnInit(): void {
    // This is intentional
  }

  filter(filter: string) {
    this.teamListData.filter(filter);
  }

  reset() {
    this.filterString = '';
    this.teamListData.filter('');
  }

}
