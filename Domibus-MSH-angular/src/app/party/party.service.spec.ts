import {inject, TestBed} from "@angular/core/testing";

import {PartyService} from "./party.service";

describe('PartyService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PartyService]
    });
  });

  it('should be created', inject([PartyService], (service: PartyService) => {
    expect(service).toBeTruthy();
  }));
});
