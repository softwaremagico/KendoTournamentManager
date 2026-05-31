import {MatSpinnerOverlayComponent} from './mat-spinner-overlay.component';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {ChangeDetectorRef} from '@angular/core';

describe('MatSpinnerOverlayComponent', () => {
  let component: MatSpinnerOverlayComponent;
  let systemOverloadService: SystemOverloadService;
  let changeDetectorRef: jasmine.SpyObj<ChangeDetectorRef>;

  beforeEach(() => {
    systemOverloadService = new SystemOverloadService();
    changeDetectorRef = jasmine.createSpyObj('ChangeDetectorRef', ['detectChanges']);

    component = new MatSpinnerOverlayComponent(systemOverloadService, changeDetectorRef);
  });

  afterEach(() => {
    component.ngOnDestroy();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default input values', () => {
    expect(component.value).toBe(100);
    expect(component.diameter).toBe(100);
    expect(component.mode).toBe('indeterminate');
    expect(component.strokeWidth).toBe(10);
    expect(component.overlay).toBeFalse();
    expect(component.color).toBe('primary');
  });

  it('should have initial spinner state as hidden', () => {
    expect(component.showSpinner).toBeFalse();
    expect(component.waitBigOperation).toBeFalse();
  });

  it('should show spinner when systemOverloadService.isBusy emits true', () => {
    component.ngOnInit();
    systemOverloadService.isBusy.next(true);

    expect(component.showSpinner).toBeTrue();
    expect(changeDetectorRef.detectChanges).toHaveBeenCalled();
  });

  it('should hide spinner when systemOverloadService.isBusy emits false', () => {
    component.ngOnInit();
    systemOverloadService.isBusy.next(true);
    expect(component.showSpinner).toBeTrue();

    systemOverloadService.isBusy.next(false);

    expect(component.showSpinner).toBeFalse();
  });

  it('should set waitBigOperation when systemOverloadService.isTransactionalBusy emits true', () => {
    component.ngOnInit();
    systemOverloadService.isTransactionalBusy.next(true);

    expect(component.waitBigOperation).toBeTrue();
    expect(changeDetectorRef.detectChanges).toHaveBeenCalled();
  });

  it('should clear waitBigOperation when systemOverloadService.isTransactionalBusy emits false', () => {
    component.ngOnInit();
    systemOverloadService.isTransactionalBusy.next(true);
    expect(component.waitBigOperation).toBeTrue();

    systemOverloadService.isTransactionalBusy.next(false);

    expect(component.waitBigOperation).toBeFalse();
  });

  it('should handle multiple state changes', () => {
    component.ngOnInit();

    systemOverloadService.isBusy.next(true);
    expect(component.showSpinner).toBeTrue();

    systemOverloadService.isBusy.next(false);
    expect(component.showSpinner).toBeFalse();

    systemOverloadService.isTransactionalBusy.next(true);
    expect(component.waitBigOperation).toBeTrue();

    systemOverloadService.isTransactionalBusy.next(false);
    expect(component.waitBigOperation).toBeFalse();
  });

  it('should allow custom input values', () => {
    component.value = 75;
    component.diameter = 50;
    component.mode = 'determinate';
    component.strokeWidth = 5;
    component.overlay = true;
    component.color = 'accent';

    expect(component.value).toBe(75);
    expect(component.diameter).toBe(50);
    expect(component.mode).toBe('determinate');
    expect(component.strokeWidth).toBe(5);
    expect(component.overlay).toBeTrue();
    expect(component.color).toBe('accent');
  });

  it('should show both spinner and wait operation concurrently', () => {
    component.ngOnInit();

    systemOverloadService.isBusy.next(true);
    systemOverloadService.isTransactionalBusy.next(true);

    expect(component.showSpinner).toBeTrue();
    expect(component.waitBigOperation).toBeTrue();
  });

  it('should unsubscribe from services on destroy', () => {
    component.ngOnInit();
    spyOn(component['destroySubject'], 'next');

    component.ngOnDestroy();

    expect(component['destroySubject'].next).toHaveBeenCalled();
  });
});

