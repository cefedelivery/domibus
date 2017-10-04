import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {PageHelperComponent} from "./page-helper.component";

describe('PageHelperComponent', () => {
  let component: PageHelperComponent;
  let fixture: ComponentFixture<PageHelperComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PageHelperComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageHelperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
