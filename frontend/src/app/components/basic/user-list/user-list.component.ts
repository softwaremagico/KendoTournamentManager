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
  filterString: string;

  constructor(private filterResetService: FilterResetService) {
  }

  ngOnInit(): void {
    this.filterResetService.resetFilter.pipe().subscribe(value => {
      if (value) {
        this.reset();
      }
    });
  }

  filter(event: Event) {
    const filter: string = (event.target as HTMLInputElement).value.toLowerCase();
    this.userListData.filter(filter);
  }

  reset() {
    this.filterString = '';
    this.userListData.filter('');
  }
}
