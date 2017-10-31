import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RollbackDirtyDialogComponent } from './rollback-dirty-dialog.component';

describe('RollbackDirtyDialogComponent', () => {
  let component: RollbackDirtyDialogComponent;
  let fixture: ComponentFixture<RollbackDirtyDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RollbackDirtyDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RollbackDirtyDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
