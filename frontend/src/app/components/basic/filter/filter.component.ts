import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Subject} from "rxjs";

@Component({
  selector: 'app-filter',
  templateUrl: './filter.component.html',
  styleUrls: ['./filter.component.scss']
})
export class FilterComponent implements OnInit {

  filterString: string;

  @Output() filterChanged: EventEmitter<string> = new EventEmitter();

  @Input() disabled: boolean;

  @Input() resetValue: Subject<boolean>;

  ngOnInit() {
    this.resetValue.subscribe(value => {
      this.reset();
    });
  }

  filter(event: Event) {
    const filter: string = (event.target as HTMLInputElement).value.toLowerCase();
    this.filterChanged.emit(filter);
  }

  reset() {
    this.filterString = '';
    this.filterChanged.emit(this.filterString);
  }
}
