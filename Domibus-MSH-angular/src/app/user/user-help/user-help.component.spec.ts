import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {UserHelpComponent} from "./user-help.component";

describe('UserHelpComponent', () => {
  let component: UserHelpComponent;
  let fixture: ComponentFixture<UserHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserHelpComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
