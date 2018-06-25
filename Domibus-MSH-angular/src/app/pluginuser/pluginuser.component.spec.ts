import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PluginUserComponent } from './pluginuser.component';

describe('PluginUserComponent', () => {
  let component: PluginUserComponent;
  let fixture: ComponentFixture<PluginUserComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PluginUserComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PluginUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
