import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import 'rxjs/add/operator/map';
import {DomibusInfo} from './domibusinfo';
import {SupportTeamInfo} from "../../security/not-authorized/supportteaminfo";

@Injectable()
export class DomibusInfoService {

  private isFourCornerEnabledPromise: Promise<boolean>;
  private isExtAuthProviderEnabledPromise: Promise<boolean>;
  private domibusInfo: Promise<DomibusInfo>;
  private supportTeamInfo: Promise<SupportTeamInfo>;

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

  getSupportTeamInfo(): Promise<SupportTeamInfo> {
    if (!this.supportTeamInfo) {
      this.supportTeamInfo = this.http.get('rest/application/supportteam')
        .map((res: Response) => res.json()).toPromise();
    }
    return this.supportTeamInfo;
  }
}
