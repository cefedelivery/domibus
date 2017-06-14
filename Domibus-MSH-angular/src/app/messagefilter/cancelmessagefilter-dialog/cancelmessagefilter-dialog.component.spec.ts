import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CancelMessagefilterDialogComponent } from './cancelmessagefilter-dialog.component';

describe('CancelMessageFilterDialogComponent', () => {
  let component: CancelMessagefilterDialogComponent;
  let fixture: ComponentFixture<CancelMessagefilterDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CancelMessagefilterDialogComponent ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CancelMessagefilterDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

