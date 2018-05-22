import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PmodeUploadComponent } from './pmode-upload.component';

describe('PmodeUploadComponent', () => {
  let component: PmodeUploadComponent;
  let fixture: ComponentFixture<PmodeUploadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PmodeUploadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PmodeUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
