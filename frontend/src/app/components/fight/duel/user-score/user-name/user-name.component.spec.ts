import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {MatIconModule} from '@angular/material/icon';
import {RouterTestingModule} from '@angular/router/testing';

import {UserNameComponent} from './user-name.component';
import {Participant} from '../../../../../models/participant';
import {Duel} from '../../../../../models/duel';
import {Fight} from '../../../../../models/fight';
import {NameUtilsService} from '../../../../../services/name-utils.service';
import {MembersOrderChangedService} from '../../../../../services/notifications/members-order-changed.service';

describe('UserNameComponent', () => {
  let userNameHostComponent: UserNameHostComponent;
  let fixture: ComponentFixture<UserNameHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        DragDropModule,
        MatIconModule,
        RouterTestingModule
      ],
      declarations: [UserNameComponent, UserNameHostComponent],
      providers: [NameUtilsService, MembersOrderChangedService]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserNameHostComponent);
    userNameHostComponent = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the host component', () => {
    expect(userNameHostComponent).toBeTruthy();
  });

  it('should render the participant name in the medium-width display format', () => {
    const participant: Participant = new Participant();
    participant.name = 'name';
    participant.lastname = 'of family';

    spyOnProperty(window, 'innerWidth').and.returnValue(1300);
    window.dispatchEvent(new Event('resize'));

    userNameHostComponent.participant = participant;
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.user-name-text').innerText.trim()).toEqual('of Family, N.');
  });

  it('should render the participant short last name on narrow screens', () => {
    const participant: Participant = new Participant();
    participant.name = 'name';
    participant.lastname = 'of Royal Family';

    spyOnProperty(window, 'innerWidth').and.returnValue(950);
    window.dispatchEvent(new Event('resize'));

    userNameHostComponent.participant = participant;
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.user-name-text').innerText.trim()).toEqual('of Royal');
  });

  @Component({
    selector: 'user-name-host-component',
    template: `
      <user-name
        [participant]="participant"
        [fight]="fight"
        [duel]="duel"
        [memberIndex]="0"
        [left]="true"
        [swapTeams]="false"
        [over]="false"
      ></user-name>`
  })
  class UserNameHostComponent {
    participant: Participant | undefined;
    fight: Fight = {duels: []} as unknown as Fight;
    duel: Duel = {finished: false} as unknown as Duel;
  }
});
