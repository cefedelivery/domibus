import { TestBed, inject } from '@angular/core/testing';

import { PluginUserService } from './pluginuser.service';

describe('PluginUserService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PluginUserService]
    });
  });

  it('should be created', inject([PluginUserService], (service: PluginUserService) => {
    expect(service).toBeTruthy();
  }));
});
