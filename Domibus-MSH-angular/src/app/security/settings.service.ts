import {Injectable} from '@angular/core';
import {Http, Headers, Response} from '@angular/http';

@Injectable()
export class SettingsService {
  constructor (private http: Http) {
  }

  isMultiDomain() {
    return true; // mockup - TODO
  }
}
