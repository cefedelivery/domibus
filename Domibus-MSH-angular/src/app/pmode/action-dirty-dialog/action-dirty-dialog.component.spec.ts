import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionDirtyDialogComponent } from './action-dirty-dialog.component';

describe('ActionDirtyDialogComponent', () => {
  let component: ActionDirtyDialogComponent;
  let fixture: ComponentFixture<ActionDirtyDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ActionDirtyDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActionDirtyDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
