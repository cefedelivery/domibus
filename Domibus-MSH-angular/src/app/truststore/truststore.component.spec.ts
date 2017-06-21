import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TruststoreComponent } from './truststore.component';

describe('TruststoreComponent', () => {
  let component: TruststoreComponent;
  let fixture: ComponentFixture<TruststoreComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TruststoreComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TruststoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
