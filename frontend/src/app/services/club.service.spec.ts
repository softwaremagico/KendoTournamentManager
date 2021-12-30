import { TestBed } from '@angular/core/testing';

import { ClubService } from './club.service';

describe('ClubService', () => {
  let service: ClubService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClubService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
