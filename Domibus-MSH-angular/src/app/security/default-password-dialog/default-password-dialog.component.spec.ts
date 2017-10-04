import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DefaultPasswordDialogComponent } from './default-password-dialog.component';

describe('DefaultPasswordDialogComponent', () => {
  let component: DefaultPasswordDialogComponent;
  let fixture: ComponentFixture<DefaultPasswordDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DefaultPasswordDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DefaultPasswordDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
