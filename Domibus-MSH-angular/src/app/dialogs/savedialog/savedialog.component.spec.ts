import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MessagefilterDialogComponent } from './savedialog.component';

describe('MessagelogDialogComponent', () => {
  let component: MessagefilterDialogComponent;
  let fixture: ComponentFixture<MessagefilterDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MessagefilterDialogComponent ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MessagefilterDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

