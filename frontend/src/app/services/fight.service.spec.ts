import { TestBed } from '@angular/core/testing';

import { FightService } from './fight.service';

describe('FightService', () => {
  let service: FightService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FightService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
