import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
import {PartyResponseRo} from "./party";
import {Observable} from "rxjs/Observable";

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Injectable()
export class PartyService {

  static readonly LIST_PARTIES: string = "rest/party/list";
  static readonly COUNT_PARTIES: string = "rest/party/count";

  constructor(private http: Http, private alertService: AlertService) {

  }

  listParties(name: string, endPoint: string, partyId: string, process: string, pageStart, pageSize): Observable<PartyResponseRo[]> {

    let searchParams: URLSearchParams = new URLSearchParams();

    searchParams.set('name', name);
    searchParams.set('endPoint', endPoint);
    searchParams.set('partyId', partyId);
    searchParams.set('process', process);
    searchParams.set('pageStart', pageStart);
    searchParams.set('pageSize', pageSize);

    return this.http.get(PartyService.LIST_PARTIES, {search: searchParams}).map(res => res.json());
  }

  countParty(name: string, endPoint: string, partyId: string, process: string): Observable<number> {
    let searchParams: URLSearchParams = new URLSearchParams();

    searchParams.set('name', name);
    searchParams.set('endPoint', endPoint);
    searchParams.set('partyId', partyId);
    searchParams.set('process', process);

    return this.http.get(PartyService.COUNT_PARTIES, {search: searchParams}).map(res => res.json());
  }

}
