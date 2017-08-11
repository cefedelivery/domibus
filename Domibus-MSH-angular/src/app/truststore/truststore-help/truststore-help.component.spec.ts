import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {TruststoreHelpComponent} from "./truststore-help.component";

describe('TruststoreHelpComponent', () => {
  let component: TruststoreHelpComponent;
  let fixture: ComponentFixture<TruststoreHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TruststoreHelpComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TruststoreHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
