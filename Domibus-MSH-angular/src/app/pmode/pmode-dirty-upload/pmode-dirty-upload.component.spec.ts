import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import {PmodeDirtyUploadComponent} from "./pmode-dirty-upload.component";

describe('PmodeDirtyUploadComponent', () => {
  let component: PmodeDirtyUploadComponent;
  let fixture: ComponentFixture<PmodeDirtyUploadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PmodeDirtyUploadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PmodeDirtyUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
