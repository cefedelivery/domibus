import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
import {PartyResponseRo} from "./party";
import {Observable} from "rxjs/Observable";
import {DownloadService} from "../download/download.service";

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Injectable()
export class PartyService {

  static readonly LIST_PARTIES: string = "rest/party/list";
  static readonly UPDATE_PARTIES: string = "rest/party/update";
  static readonly COUNT_PARTIES: string = "rest/party/count";
  static readonly CSV_PARTIES: string = "rest/party/csv";

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

  getFilterPath(name: string, endPoint: string, partyId: string, process: string) {
    let result = '?';
    //filters
    if(name) {
      result += 'name=' + name + '&';
    }
    if(endPoint) {
      result += 'endPoint=' + endPoint + '&';
    }
    if(partyId) {
      result += 'partyId=' + partyId + '&';
    }
    if(process) {
      result += 'process=' + process + '&';
    }
    return result;
  }

  saveAsCsv(name: string, endPoint: string, partyId: string, process: string) {
    DownloadService.downloadNative(PartyService.CSV_PARTIES + this.getFilterPath(name, endPoint, partyId, process));
  }

  initParty() {
    const newParty = new PartyResponseRo();
    newParty.name = 'new';
    newParty.processesWithPartyAsInitiator = [];
    newParty.processesWithPartyAsResponder = [];
    newParty.identifiers = [];
    return newParty;
  }

  updateParties(newParties: PartyResponseRo[], updatedParties: PartyResponseRo[], deletedParties: PartyResponseRo[]) {
    // new parties: keep only parties that are not deleted
    newParties = newParties.filter(x => deletedParties.indexOf(x) < 0);
    // updated parties: keep only parties that are not new or deleted
    updatedParties = updatedParties.filter(x => deletedParties.indexOf(x) < 0 && newParties.indexOf(x) < 0);

    const payload = {'added': newParties, 'updated': updatedParties, 'deleted': deletedParties };
    return this.http.put(PartyService.UPDATE_PARTIES, payload);
  }
}
