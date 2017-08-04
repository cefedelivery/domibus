import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MessagelogDetailsComponent } from './messagelog-details.component';

describe('MessagelogDetailsComponent', () => {
  let component: MessagelogDetailsComponent;
  let fixture: ComponentFixture<MessagelogDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MessagelogDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MessagelogDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
