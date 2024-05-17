import {Component, Input, OnInit} from '@angular/core';
import {UserListData} from "./user-list-data";
import {FilterResetService} from "../../../services/notifications/filter-reset.service";

@Component({
  selector: 'user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {

  @Input()
  userListData: UserListData;

  @Input()
  showAvatars: boolean = false;

  constructor(private filterResetService: FilterResetService) {
  }

  ngOnInit(): void {
    this.filterResetService.resetFilter.pipe().subscribe((value: boolean): void => {
      if (value) {
        this.filter('');
      }
    });
  }

  filter(filterString: string): void {
    this.userListData.filter(filterString);
  }
}
