import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PmodeViewComponent } from './pmode-view.component';

describe('PmodeViewComponent', () => {
  let component: PmodeViewComponent;
  let fixture: ComponentFixture<PmodeViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PmodeViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PmodeViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
