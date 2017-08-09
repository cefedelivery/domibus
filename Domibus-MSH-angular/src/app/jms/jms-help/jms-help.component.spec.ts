import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {JmsHelpComponent} from "./jms-help.component";

describe('JmsHelpComponent', () => {
  let component: JmsHelpComponent;
  let fixture: ComponentFixture<JmsHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [JmsHelpComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JmsHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
