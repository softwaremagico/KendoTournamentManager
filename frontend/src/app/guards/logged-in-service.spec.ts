import { TestBed } from '@angular/core/testing';

import { LoggedInServiceService } from './logged-in.service';

describe('LoggedInServiceService', () => {
  let service: LoggedInServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoggedInServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
