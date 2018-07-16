import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RestoreDialogComponent } from './restore-dialog.component';

describe('RestoreDialogComponent', () => {
  let component: RestoreDialogComponent;
  let fixture: ComponentFixture<RestoreDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RestoreDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RestoreDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
