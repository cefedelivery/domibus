import {Injectable} from "@angular/core";
import {AuditCriteria, AuditResponseRo} from "./audit";
import {Observable} from "rxjs/Observable";
import {AlertService} from "../alert/alert.service";
import {Http} from "@angular/http";

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * In charge of retrieving audit information from the backend.
 */
@Injectable()
export class AuditService {

  constructor(private http: Http, private alertService: AlertService) {

  }

  listAuditLogs(auditCriteria: AuditCriteria): Observable<AuditResponseRo[]> {
    return this.http.post("rest/audit/list", auditCriteria).map(res => res.json());
  }

  countAuditLogs(auditCriteria: AuditCriteria): Observable<number> {
    return this.http.post("rest/audit/count", auditCriteria).map(res => res.json());
  }

  listTargetTypes(): Observable<string> {
    return this.http.get("rest/audit/targets")
      .flatMap(res => res.json())
      .map((auditTarget: string) => auditTarget);
  }

  listActions(): Observable<string> {
    return Observable.from(["Created", "Modified", "Deleted"]);
  }

}
