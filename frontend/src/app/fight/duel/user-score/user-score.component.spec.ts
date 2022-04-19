import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserScoreComponent} from './user-score.component';

describe('UserScoreComponent', () => {
  let component: UserScoreComponent;
  let fixture: ComponentFixture<UserScoreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserScoreComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserScoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
