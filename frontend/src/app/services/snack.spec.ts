import { TestBed } from '@angular/core/testing';

import { Snack } from './snack';

describe('Snack', () => {
  let service: Snack;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Snack);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
