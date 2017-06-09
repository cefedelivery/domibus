import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteMessagefilterDialogComponent } from './deletemessagefilter-dialog.component';

describe('DeleteMessageFilterDialogComponent', () => {
  let component: DeleteMessagefilterDialogComponent;
  let fixture: ComponentFixture<DeleteMessagefilterDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeleteMessagefilterDialogComponent ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteMessagefilterDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

