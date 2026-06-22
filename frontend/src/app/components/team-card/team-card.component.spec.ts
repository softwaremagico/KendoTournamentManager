import {ComponentFixture, TestBed} from '@angular/core/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {MatTooltipModule} from '@angular/material/tooltip';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {DragDropModule} from '@angular/cdk/drag-drop';

import {TeamCardComponent} from './team-card.component';
import {Team} from '../../models/team';
import {ParticipantNamePipe} from '../../pipes/visualization/participant-name-pipe';
import {NameUtilsService} from '../../services/name-utils.service';

describe('TeamCardComponent', () => {
  let component: TeamCardComponent;
  let fixture: ComponentFixture<TeamCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatCardModule,
        MatTooltipModule,
        DragDropModule,
        NoopAnimationsModule,
        ParticipantNamePipe
      ],
      declarations: [TeamCardComponent],
      providers: [NameUtilsService],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TeamCardComponent);
    component = fixture.componentInstance;

    const team = new Team('Team A');
    team.locked = false;
    team.members = [];
    component.team = team;

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });
});
