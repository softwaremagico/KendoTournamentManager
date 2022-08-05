import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserNameComponent} from './user-name.component';
import {Component, ViewChild} from "@angular/core";
import {Participant} from "../../../../../models/participant";

describe('UserNameComponent', () => {
  let userNameHostComponent: UserNameHostComponent;
  let fixture: ComponentFixture<UserNameHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserNameComponent, UserNameHostComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserNameHostComponent);
    userNameHostComponent = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(userNameHostComponent).toBeTruthy();
  });

  it('participant name', () => {
    let participant: Participant = new Participant();
    participant.name = "name";
    participant.lastname = "of family";
    userNameHostComponent.userNameComponent.participant = participant;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('div').innerText).toEqual('of Family, N.');
  });

  it('participant shortLastname', () => {
    let participant: Participant = new Participant();
    participant.name = "name";
    participant.lastname = "of Royal Family";

    // Resolution <900
    spyOnProperty(window, 'innerWidth').and.returnValue(1199);
    window.dispatchEvent(new Event('resize'));

    userNameHostComponent.userNameComponent.participant = participant;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('div').innerText).toEqual('of Royal');
  });

  @Component({
    selector: `user-name-host-component`,
    template: `
      <user-name></user-name>`
  })
  class UserNameHostComponent {
    @ViewChild(UserNameComponent)
    public userNameComponent: UserNameComponent;
  }
});
