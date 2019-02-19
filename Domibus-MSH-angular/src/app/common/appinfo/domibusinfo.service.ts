import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {ReplaySubject} from 'rxjs';
import {DomibusInfo} from './domibusinfo';

@Injectable()
export class DomibusInfoService {

  private isFourCornerEnabledPromise: Promise<boolean>;
  private isExtAuthProviderEnabledPromise: Promise<boolean>;
  private domibusInfo: Promise<DomibusInfo>;

  constructor(private http: Http) {
  }

  getDomibusInfo(): Promise<DomibusInfo> {
    if (!this.domibusInfo) {
      this.domibusInfo = this.http.get('rest/application/info')
        .map((res: Response) => <DomibusInfo>res.json())
        .toPromise();
    }
    return this.domibusInfo;
  }

  isFourCornerEnabled(): Promise<boolean> {
    if (!this.isFourCornerEnabledPromise) {
      this.isFourCornerEnabledPromise = this.http.get('rest/application/fourcornerenabled')
        .map((res: Response) => res.json()).toPromise();
    }
    return this.isFourCornerEnabledPromise;
  }

  isExtAuthProviderEnabled(): Promise<boolean> {
    if (!this.isExtAuthProviderEnabledPromise) {
      this.isExtAuthProviderEnabledPromise = this.http.get('rest/application/extauthproviderenabled')
        .map((res: Response) => res.json()).toPromise();
    }
    return this.isExtAuthProviderEnabledPromise;
  }
}
