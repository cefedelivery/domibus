import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditpluginuserFormComponent } from './editpluginuser-form.component';

describe('EditpluginuserFormComponent', () => {
  let component: EditpluginuserFormComponent;
  let fixture: ComponentFixture<EditpluginuserFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditpluginuserFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditpluginuserFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
