import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RollbackDialogComponent } from './rollback-dialog.component';

describe('RollbackDialogComponent', () => {
  let component: RollbackDialogComponent;
  let fixture: ComponentFixture<RollbackDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RollbackDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RollbackDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
