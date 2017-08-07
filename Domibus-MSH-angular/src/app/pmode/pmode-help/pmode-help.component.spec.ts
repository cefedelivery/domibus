import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {PmodeHelpComponent} from "./pmode-help.component";

describe('PmodeHelpComponent', () => {
  let component: PmodeHelpComponent;
  let fixture: ComponentFixture<PmodeHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PmodeHelpComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PmodeHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
