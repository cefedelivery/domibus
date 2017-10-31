import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
import {PartyResponseRo} from "./party";
import {Observable} from "rxjs/Observable";
import {Observer} from "rxjs/Observer";

@Injectable()
export class PartyService {

  constructor(private http: Http, private alertService: AlertService) {

  }

  listAuditLogs(name: string, endPoint: string, partyId: string, process: string, pageStart, pageSize): Observable<PartyResponseRo[]> {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('name', name);
    searchParams.set('endPoint', endPoint);
    searchParams.set('partyId', partyId);
    searchParams.set('process', process);
    searchParams.set('pageStart', pageStart);
    searchParams.set('pageSize', pageSize);
    return this.http.get("rest/party/list", {search: searchParams}).map(res => res.json());
  }

  countParty(name: string, endPoint: string, partyId: string, process: string): Observable<number> {
    return Observable.create((observer: Observer<number>) => {
      observer.next(10);
      observer.complete();
    });
  }

}
