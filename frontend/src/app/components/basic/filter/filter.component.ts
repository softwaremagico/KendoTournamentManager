import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-filter',
  templateUrl: './filter.component.html',
  styleUrls: ['./filter.component.scss']
})
export class FilterComponent {

  filterString: string;

  @Output() filterChanged: EventEmitter<string> = new EventEmitter();

  @Input() disabled: boolean;

  filter(event: Event) {
    const filter: string = (event.target as HTMLInputElement).value.toLowerCase();
    this.filterChanged.emit(filter);
  }

  reset() {
    this.filterString = '';
    this.filterChanged.emit(this.filterString);
  }
}
