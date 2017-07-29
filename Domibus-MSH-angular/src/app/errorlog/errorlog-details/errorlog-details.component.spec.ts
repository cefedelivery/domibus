import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorlogDetailsComponent } from './errorlog-details.component';

describe('ErrorlogDetailsComponent', () => {
  let component: ErrorlogDetailsComponent;
  let fixture: ComponentFixture<ErrorlogDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ErrorlogDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorlogDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
