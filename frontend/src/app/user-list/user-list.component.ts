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

  constructor() { }

  ngOnInit(): void {
  }

}
