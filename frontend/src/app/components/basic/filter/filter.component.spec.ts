import {Subject} from 'rxjs';

import {FilterComponent} from './filter.component';
import {FilterFocusService} from '../../../services/notifications/filter-focus.service';
import {FilterResetService} from '../../../services/notifications/filter-reset.service';

describe('FilterComponent', () => {
  let component: FilterComponent;
  let filterResetService: FilterResetService;
  let filterFocusService: FilterFocusService;

  beforeEach(() => {
    filterResetService = new FilterResetService();
    filterFocusService = new FilterFocusService();
    component = new FilterComponent(filterResetService, filterFocusService);
    component.resetValue = new Subject<boolean>();
  });

  it('should emit the filter text in lowercase when filterValue is called', () => {
    spyOn(component.filterChanged, 'emit');

    component.filterValue('HeLLo World');

    expect(component.filterChanged.emit).toHaveBeenCalledOnceWith('hello world');
  });

  it('should read the input value and emit it in lowercase when filtering from an event', () => {
    spyOn(component.filterChanged, 'emit');
    const event = {
      target: {
        value: 'KeNdO'
      }
    } as unknown as Event;

    component.filter(event);

    expect(component.filterChanged.emit).toHaveBeenCalledOnceWith('kendo');
  });

  it('should clear the current filter string and emit an empty value when reset is called', () => {
    spyOn(component.filterChanged, 'emit');
    component.filterString = 'active filter';

    component.reset();

    expect(component.filterString).toBe('');
    expect(component.filterChanged.emit).toHaveBeenCalledOnceWith('');
  });

  it('should reset when the resetValue input emits', () => {
    spyOn(component.filterChanged, 'emit');
    component.ngOnInit();
    (component.filterChanged.emit as jasmine.Spy).calls.reset();
    component.filterString = 'temporary value';

    component.resetValue.next(true);

    expect(component.filterString).toBe('');
    expect(component.filterChanged.emit).toHaveBeenCalledOnceWith('');
  });

  it('should reset when the reset service emits', () => {
    spyOn(component.filterChanged, 'emit');
    component.ngOnInit();
    (component.filterChanged.emit as jasmine.Spy).calls.reset();
    component.filterString = 'temporary value';

    filterResetService.resetFilter.next(true);

    expect(component.filterString).toBe('');
    expect(component.filterChanged.emit).toHaveBeenCalledOnceWith('');
  });

  it('should mark the filter as active on focus and inactive on focusout', () => {
    component.focus();
    expect(filterFocusService.isFilterActive.value).toBeTrue();

    component.focusout();
    expect(filterFocusService.isFilterActive.value).toBeFalse();
  });
});

