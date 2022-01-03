import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubListComponent } from './club-list.component';

describe('ClubListComponent', () => {
  let component: ClubListComponent;
  let fixture: ComponentFixture<ClubListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClubListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ClubListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // @ts-ignore
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
function beforeEach(arg0: () => Promise<void>) {
    throw new Error('Function not implemented.');
}

