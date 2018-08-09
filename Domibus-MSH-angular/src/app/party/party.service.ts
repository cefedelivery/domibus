import {Injectable} from '@angular/core';
import {Http, Response, URLSearchParams} from '@angular/http';
import {AlertService} from 'app/alert/alert.service';
import {PartyResponseRo, PartyFilteredResult, ProcessRo, CertificateRo} from './party';
import {Observable} from 'rxjs/Observable';
import {DownloadService} from '../download/download.service';

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Injectable()
export class PartyService {
  static readonly CERTIFICATE: string = 'rest/party/{partyName}/certificate';
  static readonly LIST_PROCESSES: string = 'rest/party/processes';
  static readonly LIST_PARTIES: string = 'rest/party/list';
  static readonly UPDATE_PARTIES: string = 'rest/party/update';
  static readonly CSV_PARTIES: string = 'rest/party/csv';

  constructor (private http: Http, private alertService: AlertService) {

  }

  uploadCertificate (payload, partyName: string): Observable<CertificateRo> {
    return this.http.put(PartyService.CERTIFICATE.replace('{partyName}', partyName), payload)
      .map(res => res.json());
  }

  getCertificate (partyName: string): Observable<CertificateRo> {
    return this.http.get(PartyService.CERTIFICATE.replace('{partyName}', partyName))
      .map(res => res.json());
  }

  listProcesses (): Observable<ProcessRo> {
    return this.http.get(PartyService.LIST_PROCESSES)
      .map(res => res.json()).catch(() => Observable.throw("No processes found"));
  }

  listParties (name: string, endPoint: string, partyId: string, process: string)
    : Observable<PartyFilteredResult> {

    return this.http.get(PartyService.LIST_PARTIES).map(res => {
      const allRecords = res.json() as PartyResponseRo[];
      let records = allRecords.slice();

      if (name) {
        records = records.filter(party => party.name === name);
      }
      if (endPoint) {
        records = records.filter(party => party.endpoint === endPoint);
      }
      if (partyId) {
        records = records.filter(party => party.identifiers.filter(x => x.partyId === partyId).length > 0);
      }
      if (process) {
        records = records.filter(party => party.joinedProcesses.lastIndexOf(process) >= 0);
      }

      return {data: records, allData: allRecords};
    }).catch(() => Observable.throw("No parties found"));

  }

  getFilterPath (name: string, endPoint: string, partyId: string, process: string) {
    let result = '?';

    if (name) {
      result += 'name=' + name + '&';
    }
    if (endPoint) {
      result += 'endPoint=' + endPoint + '&';
    }
    if (partyId) {
      result += 'partyId=' + partyId + '&';
    }
    if (process) {
      result += 'process=' + process + '&';
    }
    return result;
  }

  saveAsCsv (name: string, endPoint: string, partyId: string, process: string) {
    DownloadService.downloadNative(PartyService.CSV_PARTIES + this.getFilterPath(name, endPoint, partyId, process));
  }

  initParty () {
    const newParty = new PartyResponseRo();
    newParty.name = 'new';
    newParty.processesWithPartyAsInitiator = [];
    newParty.processesWithPartyAsResponder = [];
    newParty.identifiers = [];
    return newParty;
  }

  updateParties (partyList: PartyResponseRo[]) {
    return this.http.put(PartyService.UPDATE_PARTIES, partyList).toPromise();
  }
}
