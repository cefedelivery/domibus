import {Injectable} from "@angular/core";
import {Http, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import "rxjs/add/operator/map";
import {ReplaySubject} from "rxjs";
import {DomibusInfo} from "./domibusinfo";

@Injectable()
export class DomibusInfoService {

  private isFourCornerEnabledPromise: Promise<boolean>;
  private isExtAuthProviderEnabledPromise: Promise<boolean>;

  constructor(private http: Http) {
  }


  getDomibusInfo(): Observable<DomibusInfo> {
    let subject = new ReplaySubject();
    this.http.get('rest/application/info')
      .map((response: Response) => {
        let domibusInfo = new DomibusInfo(response.json().version);
        return domibusInfo;
      })
      .subscribe((res: DomibusInfo) => {
        subject.next(res);
      }, (error: any) => {
        // console.log("getDomibusInfo:" + error);
      });
    return subject.asObservable();
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
