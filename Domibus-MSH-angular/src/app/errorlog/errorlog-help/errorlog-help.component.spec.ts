import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {ErrorlogHelpComponent} from "./errorlog-help.component";

describe('ErrorlogHelpComponent', () => {
  let component: ErrorlogHelpComponent;
  let fixture: ComponentFixture<ErrorlogHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ErrorlogHelpComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorlogHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
