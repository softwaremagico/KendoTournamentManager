import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubDialogBoxComponent } from './club-dialog-box.component';

describe('DialogBoxComponent', () => {
  let component: ClubDialogBoxComponent;
  let fixture: ComponentFixture<ClubDialogBoxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClubDialogBoxComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ClubDialogBoxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
