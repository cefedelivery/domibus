import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JmsComponent } from './jms.component';

describe('JmsComponent', () => {
  let component: JmsComponent;
  let fixture: ComponentFixture<JmsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JmsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JmsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
