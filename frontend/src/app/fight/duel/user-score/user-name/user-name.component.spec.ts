import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserNameComponent} from './user-name.component';
import {Component, ViewChild} from "@angular/core";
import {Participant} from "../../../../models/participant";

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
    participant.name = "Name";
    participant.lastname = "Familyname";
    userNameHostComponent.userNameComponent.participant = participant;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('div').innerText).toEqual('N. Familyname');
  });

  @Component({
    selector: `user-name-host-component`,
    template: `<user-name></user-name>`
  })
  class UserNameHostComponent {
    @ViewChild(UserNameComponent)
    public userNameComponent: UserNameComponent;
  }
});
