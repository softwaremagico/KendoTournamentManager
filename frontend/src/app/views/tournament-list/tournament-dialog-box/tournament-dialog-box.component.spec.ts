import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TournamentDialogBoxComponent } from './tournament-dialog-box.component';

describe('TournamentDialogBoxComponent', () => {
  let component: TournamentDialogBoxComponent;
  let fixture: ComponentFixture<TournamentDialogBoxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TournamentDialogBoxComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TournamentDialogBoxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
