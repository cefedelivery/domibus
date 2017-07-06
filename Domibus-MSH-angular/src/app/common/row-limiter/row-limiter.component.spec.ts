import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RowLimiterComponent } from './row-limiter.component';

describe('RowLimiterComponent', () => {
  let component: RowLimiterComponent;
  let fixture: ComponentFixture<RowLimiterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RowLimiterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RowLimiterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
