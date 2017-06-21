import {Injectable} from "@angular/core";
import {Http, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import "rxjs/add/operator/map";
import {Router} from "@angular/router";
import {HttpEventService} from "../http/http.event.service";
import {ReplaySubject} from "rxjs";
import {DomibusInfo} from "./domibusinfo";

@Injectable()
export class DomibusInfoService {
  constructor(private http: Http, private router: Router) {
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
        console.log("getDomibusInfo:" + error);
        // subject.next(null);
      });
    return subject.asObservable();
  }

}
