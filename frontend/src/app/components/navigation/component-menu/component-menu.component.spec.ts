import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComponentMenuComponent } from './component-menu.component';

describe('BiitComponentMenuComponent', () => {
  let component: ComponentMenuComponent;
  let fixture: ComponentFixture<ComponentMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ComponentMenuComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ComponentMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
