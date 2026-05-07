import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FloatingDeleteButtonComponent } from './floating-delete-button.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('FloatingDeleteButtonComponent', () => {
  let component: FloatingDeleteButtonComponent;
  let fixture: ComponentFixture<FloatingDeleteButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FloatingDeleteButtonComponent ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FloatingDeleteButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default checked input as false', () => {
    expect(component.checked).toBeFalse();
  });

  it('should set checked input when provided', () => {
    component.checked = true;
    expect(component.checked).toBeTrue();
  });

  it('should set icon input when provided', () => {
    const testIcon = { type: 'material', name: 'delete' } as any;
    component.icon = testIcon;
    expect(component.icon).toEqual(testIcon);
  });

  it('should toggle checked property', () => {
    component.checked = false;
    expect(component.checked).toBeFalse();

    component.checked = true;
    expect(component.checked).toBeTrue();

    component.checked = false;
    expect(component.checked).toBeFalse();
  });
});

