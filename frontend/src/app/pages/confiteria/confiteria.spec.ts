import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Confiteria } from './confiteria';

describe('Confiteria', () => {
  let component: Confiteria;
  let fixture: ComponentFixture<Confiteria>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Confiteria],
    }).compileComponents();

    fixture = TestBed.createComponent(Confiteria);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
