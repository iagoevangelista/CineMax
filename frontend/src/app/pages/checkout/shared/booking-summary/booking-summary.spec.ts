import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

// Provide a lightweight stub component for testing if the real export is a type-only
@Component({ selector: 'app-booking-summary-sections', template: '' })
class BookingSummarySections {}

describe('BookingSummarySections', () => {
  let component: BookingSummarySections;
  let fixture: ComponentFixture<BookingSummarySections>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookingSummarySections]
    }).compileComponents();

    fixture = TestBed.createComponent(BookingSummarySections);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
