import {Component, Input, OnInit} from '@angular/core';
import {TeamListData} from "./team-list-data";
import {Team} from "../../../models/team";

@Component({
  selector: 'team-list',
  templateUrl: './team-list.component.html',
  styleUrls: ['./team-list.component.scss']
})
export class TeamListComponent implements OnInit {

  @Input()
  teamListData: TeamListData;

  @Input()
  minify: boolean = false;

  @Input()
  horizontal: boolean = false;

  @Input()
  grid: boolean = false;

  @Input()
  teamsDragDisabled: Team[] = [];

  ngOnInit(): void {
    // This is intentional
  }

  filter(filter: string) {
    this.teamListData.filter(filter);
  }

  reset() {
    this.teamListData.filter('');
  }

  public isDragDisabled(team: Team): boolean {
    return this.teamsDragDisabled && this.teamsDragDisabled.indexOf(team) >= 0;
  }

}
