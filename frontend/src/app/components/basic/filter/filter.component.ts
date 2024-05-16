import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Subject} from "rxjs";
import {FilterResetService} from "../../../services/notifications/filter-reset.service";
import {FilterFocusService} from "../../../services/notifications/filter-focus.service";

@Component({
  selector: 'app-filter',
  templateUrl: './filter.component.html',
  styleUrls: ['./filter.component.scss']
})
export class FilterComponent implements OnInit {

  filterString: string;

  @Output() filterChanged: EventEmitter<string> = new EventEmitter();

  @Input() disabled: boolean;

  @Input() resetValue: Subject<boolean> = new Subject<boolean>();

  constructor(private filterResetService: FilterResetService, private filterFocusService: FilterFocusService) {
  }

  ngOnInit(): void {
    this.resetValue.subscribe((): void => {
      this.reset();
    });
    this.filterResetService.resetFilter.subscribe((): void => {
      this.reset();
    })
  }

  filter(event: Event): void {
    const filter: string = (event.target as HTMLInputElement).value.toLowerCase();
    this.filterChanged.emit(filter);
  }

  reset(): void {
    this.filterString = '';
    this.filterChanged.emit(this.filterString);
  }

  focus(): void {
    this.filterFocusService.isFilterActive.next(true);
  }

  focusout(): void {
    this.filterFocusService.isFilterActive.next(false);
  }
}
