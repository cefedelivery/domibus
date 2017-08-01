import {Http, Response} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {TrustStoreEntry} from "./trustore.model";
/**
 * Created by dussath on 8/1/17.
 */

@Injectable()
export class TrustStoreService {

  constructor(private http: Http, private alertService: AlertService) {

  }

  getEntries(): Observable<TrustStoreEntry[]> {
    return this.http.get("rest/truststore/list")
      .map(this.extractData)
      .catch(this.handleError);
  }

  private extractData(res: Response) {
    let body = res.json();
    return body || {};
  }

  private handleError(error: Response | any) {
    this.alertService.error(error, false);
    let errMsg: string;
    if (error instanceof Response) {
      const body = error.json() || '';
      const err = body.error || JSON.stringify(body);
      errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    console.error(errMsg);
    return Promise.reject(errMsg);
  }

}
