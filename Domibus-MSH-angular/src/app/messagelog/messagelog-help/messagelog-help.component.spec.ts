import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {MessagelogHelpComponent} from "./messagelog-help.component";

describe('MessagelogHelpComponent', () => {
  let component: MessagelogHelpComponent;
  let fixture: ComponentFixture<MessagelogHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MessagelogHelpComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MessagelogHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
