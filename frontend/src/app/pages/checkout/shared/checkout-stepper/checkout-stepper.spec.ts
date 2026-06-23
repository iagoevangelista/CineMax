import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckoutStep, CheckoutStepperComponent } from './checkout-stepper';

describe('CheckoutStepp erComponent', () => {
  let component: CheckoutStepperComponent;
  let fixture: ComponentFixture<CheckoutStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CheckoutStepperComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(CheckoutStepperComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
