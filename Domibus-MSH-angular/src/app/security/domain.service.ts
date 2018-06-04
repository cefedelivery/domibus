import {Injectable} from '@angular/core';
import {Http, Headers, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {ReplaySubject} from 'rxjs';
import 'rxjs/add/operator/map';
import {Domain} from './domain';

@Injectable()
export class DomainService {

  static readonly MULTI_TENANCY_URL: string = 'rest/application/multitenancy';
  static readonly CURRENT_DOMAIN_URL: string = 'rest/security/user/domain';
  static readonly DOMAIN_LIST_URL: string = 'rest/application/domains';

  private isMultiDomainSubject: ReplaySubject<boolean>;
  private domainSubject: ReplaySubject<Domain>;

  constructor (private http: Http) {
  }

  isMultiDomain (): Observable<boolean> {
    if (!this.isMultiDomainSubject) {
      this.isMultiDomainSubject = new ReplaySubject<boolean>();
      this.http.get(DomainService.MULTI_TENANCY_URL).subscribe((res: Response) => {
        this.isMultiDomainSubject.next(res.json());
      }, (error: any) => {
        console.log('get isMultiDomain:' + error);
        this.isMultiDomainSubject.next(false);
      });
    }
    return this.isMultiDomainSubject.asObservable();
  }

  getCurrentDomain (): Observable<Domain> {
    if (!this.domainSubject) {
      this.domainSubject = new ReplaySubject<Domain>();
      this.http.get(DomainService.CURRENT_DOMAIN_URL).subscribe((res: Response) => {
        this.domainSubject.next(res.json());
      }, (error: any) => {
        console.log('getCurrentDomain:' + error);
        this.domainSubject.next(null);
      });
    }
    return this.domainSubject.asObservable();
  }

  resetDomain (): void {
    if (this.domainSubject) {
      this.domainSubject.unsubscribe();
    }
    this.domainSubject = null;
  }

  getDomains (): Observable<Domain[]> {
    return this.http.get(DomainService.DOMAIN_LIST_URL).map((res: Response) => res.json());
  }

  setCurrentDomain (domain: Domain) {
    return this.http.put(DomainService.CURRENT_DOMAIN_URL, domain.code).toPromise().then(() => {
      if (this.domainSubject) {
        this.domainSubject.next(domain);
      }
    });
  }

}
