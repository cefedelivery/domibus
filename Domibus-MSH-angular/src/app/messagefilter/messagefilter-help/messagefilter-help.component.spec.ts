import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {MessagefilterHelpComponent} from "./messagefilter-help.component";

describe('MessagefilterHelpComponent', () => {
  let component: MessagefilterHelpComponent;
  let fixture: ComponentFixture<MessagefilterHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MessagefilterHelpComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MessagefilterHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
