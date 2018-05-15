import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {DomainSelectorComponent} from "./domain-selector.component";

describe('DomainSelectorComponent', () => {
  let component: DomainSelectorComponent;
  let fixture: ComponentFixture<DomainSelectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DomainSelectorComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
