import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FightStatisticsPanelComponent } from './fight-statistics-panel.component';

describe('FightStatisticsPanelComponent', () => {
  let component: FightStatisticsPanelComponent;
  let fixture: ComponentFixture<FightStatisticsPanelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FightStatisticsPanelComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FightStatisticsPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
