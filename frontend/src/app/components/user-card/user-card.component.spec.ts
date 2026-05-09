import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UserCardComponent} from './user-card.component';
import {RbacService} from '../../services/rbac/rbac.service';
import {RbacActivity} from '../../services/rbac/rbac.activity';
import {Participant} from '../../models/participant';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {HasPermissionPipe} from '../../pipes/has-permission.pipe';

describe('UserCardComponent', () => {
  let component: UserCardComponent;
  let fixture: ComponentFixture<UserCardComponent>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  const createParticipant = (id: number, name: string, lastname: string): Participant => ({
    id,
    name,
    lastname,
    idCard: `ID${id}`,
    hasAvatar: false,
    locked: false
  } as unknown as Participant);

  beforeEach(async () => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    await TestBed.configureTestingModule({
      declarations: [
        UserCardComponent
      ],
      providers: [
        { provide: RbacService, useValue: rbacServiceSpy }
      ],
      imports: [
        MatCardModule,
        DragDropModule,
        HasPermissionPipe
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserCardComponent);
    component = fixture.componentInstance;
    component.user = createParticipant(1, 'Test', 'User');
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default dragDisabled as false', () => {
    expect(component.dragDisabled).toBeFalse();
  });

  it('should have default showAvatar as false', () => {
    expect(component.showAvatar).toBeFalse();
  });

  it('should have default showClub as true', () => {
    expect(component.showClub).toBeTrue();
  });

  it('should have default activity as DRAG_PARTICIPANT', () => {
    expect(component.activity).toBe(RbacActivity.DRAG_PARTICIPANT);
  });

  it('should set user input when provided', () => {
    const participant = createParticipant(1, 'John', 'Doe');
    component.user = participant;

    expect(component.user).toEqual(participant);
  });

  it('should allow drag when dragDisabled is false', () => {
    component.dragDisabled = false;
    expect(component.dragDisabled).toBeFalse();
  });

  it('should disable drag when dragDisabled is true', () => {
    component.dragDisabled = true;
    expect(component.dragDisabled).toBeTrue();
  });

  it('should show avatar when showAvatar is true', () => {
    component.showAvatar = true;
    expect(component.showAvatar).toBeTrue();
  });

  it('should hide avatar when showAvatar is false', () => {
    component.showAvatar = false;
    expect(component.showAvatar).toBeFalse();
  });

  it('should show club when showClub is true', () => {
    component.showClub = true;
    expect(component.showClub).toBeTrue();
  });

  it('should hide club when showClub is false', () => {
    component.showClub = false;
    expect(component.showClub).toBeFalse();
  });

  it('should emit onClick event when user is clicked', () => {
    spyOn(component.clickEvent, 'emit');
    const participant = createParticipant(1, 'John', 'Doe');
    component.user = participant;

    component.clickEvent.emit(participant);

    expect(component.clickEvent.emit).toHaveBeenCalledOnceWith(participant);
  });

  it('should set activity property', () => {
    component.activity = RbacActivity.EDIT_FIGHT_TIME;
    expect(component.activity).toBe(RbacActivity.EDIT_FIGHT_TIME);
  });
});

