import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MessagelogDialogComponent } from './messagelog-dialog.component';

describe('MessagelogDialogComponent', () => {
  let component: MessagelogDialogComponent;
  let fixture: ComponentFixture<MessagelogDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MessagelogDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MessagelogDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
