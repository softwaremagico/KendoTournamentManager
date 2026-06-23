import {ComponentFixture, TestBed} from '@angular/core/testing';
import {RoleSelectorComponent} from './role-selector.component';
import {Tournament} from '../../models/tournament';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {TranslocoModule} from '@ngneat/transloco';

describe('RoleSelectorComponent', () => {
  let component: RoleSelectorComponent;
  let fixture: ComponentFixture<RoleSelectorComponent>;
  let tournament: Tournament;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RoleSelectorComponent ],
      imports: [ TranslocoModule ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoleSelectorComponent);
    component = fixture.componentInstance;

    tournament = {
      id: 1,
      name: 'Test Tournament'
    } as Tournament;

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize roles as empty array', () => {
    expect(component.roles).toEqual([]);
  });

  it('should have roleTypes populated from RoleType.toArray()', () => {
    expect(component.roleTypes.length).toBeGreaterThan(0);
  });

  it('should select a role when checked is true', () => {
    const roleType = component.roleTypes[0];
    component.select(true, roleType);
    expect(component.roles).toContain(roleType);
  });

  it('should deselect a role when checked is false', () => {
    const roleType = component.roleTypes[0];
    component.select(true, roleType);
    expect(component.roles).toContain(roleType);

    component.select(false, roleType);
    expect(component.roles).not.toContain(roleType);
  });

  it('should select multiple roles', () => {
    const role1 = component.roleTypes[0];
    const role2 = component.roleTypes[1];

    component.select(true, role1);
    component.select(true, role2);

    expect(component.roles.length).toBe(2);
    expect(component.roles).toContain(role1);
    expect(component.roles).toContain(role2);
  });

  it('should emit onClosed with tournament, roles and newOnes flag when setRoles is called', () => {
    spyOn(component.closed, 'emit');
    component.tournament = tournament;
    const roleType = component.roleTypes[0];
    component.select(true, roleType);

    component.setRoles(true);

    expect(component.closed.emit).toHaveBeenCalledOnceWith({
      tournament: tournament,
      roles: [roleType],
      newOnes: true
    });
  });

  it('should emit onClosed without payload when closeDialog is called', () => {
    spyOn(component.closed, 'emit');

    component.closeDialog();

    expect(component.closed.emit).toHaveBeenCalledOnceWith();
  });

  it('should emit onClosed with newOnes false when setRoles(false) is called', () => {
    spyOn(component.closed, 'emit');
    component.tournament = tournament;

    component.setRoles(false);

    expect(component.closed.emit).toHaveBeenCalledOnceWith({
      tournament: tournament,
      roles: [],
      newOnes: false
    });
  });
});

