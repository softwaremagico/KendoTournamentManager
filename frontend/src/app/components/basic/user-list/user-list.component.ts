import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {UserListData} from "./user-list-data";
import {Participant} from "../../../models/participant";

@Component({
  selector: 'user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit, OnChanges {

  @Input()
  userListData: UserListData;

  constructor() {

  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log(changes['userListData'].currentValue);
    console.log(changes['userListData'].currentValue.participants);
    this.userListData.initParticipants(changes['userListData'].currentValue.participants);
  }

  filter(event: Event) {
    const filter = (event.target as HTMLInputElement).value.toLowerCase();
    this.userListData.filter(filter);
  }
}
