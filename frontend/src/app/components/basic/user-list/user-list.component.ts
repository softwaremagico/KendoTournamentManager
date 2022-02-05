import {Component, Input, OnInit} from '@angular/core';
import {UserListData} from "./user-list-data";

@Component({
  selector: 'user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {

  @Input()
  userListData: UserListData;
  originalUsers: UserListData;

  constructor() {

  }

  ngOnInit(): void {

  }

  filter(event: Event) {
    const filter = (event.target as HTMLInputElement).value.toLowerCase();
    if (this.originalUsers === undefined) {
      this.originalUsers = Object.assign([], this.userListData);
    }
    this.userListData = Object.assign([], this.originalUsers);
    this.userListData.participants = this.userListData.participants.filter(user => user.lastname.toLowerCase().includes(filter) ||
      user.name.toLowerCase().includes(filter) || user.idCard.toLowerCase().includes(filter));
  }

}
