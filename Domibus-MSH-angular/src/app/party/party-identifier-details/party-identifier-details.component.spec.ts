import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PartyIdentifierDetailsComponent } from './party-identifier-details.component';

describe('PartyIdentifierDetailsComponent', () => {
  let component: PartyIdentifierDetailsComponent;
  let fixture: ComponentFixture<PartyIdentifierDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PartyIdentifierDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PartyIdentifierDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
