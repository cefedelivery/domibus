import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {TruststoreDialogComponent} from "./truststore-dialog.component";

describe('TruststoreDialogComponent', () => {
  let component: TruststoreDialogComponent;
  let fixture: ComponentFixture<TruststoreDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TruststoreDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TruststoreDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
