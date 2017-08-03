import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {TrustStoreUploadComponent} from "./truststore-upload.component";

describe('TrustStoreUploadComponent', () => {
  let component: TrustStoreUploadComponent;
  let fixture: ComponentFixture<TrustStoreUploadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TrustStoreUploadComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TrustStoreUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
