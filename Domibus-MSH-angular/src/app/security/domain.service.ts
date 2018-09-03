import {Injectable} from '@angular/core';
import {Http, Headers, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {AsyncSubject, BehaviorSubject, Subject} from 'rxjs';
import 'rxjs/add/operator/map';
import {Domain} from './domain';
import {Title} from '@angular/platform-browser';

@Injectable()
export class DomainService {

  static readonly MULTI_TENANCY_URL: string = 'rest/application/multitenancy';
  static readonly CURRENT_DOMAIN_URL: string = 'rest/security/user/domain';
  static readonly DOMAIN_LIST_URL: string = 'rest/application/domains';

  private isMultiDomainSubject: Subject<boolean>;
  private domainSubject: Subject<Domain>;

  constructor (private http: Http, private titleService: Title) {
  }

  isMultiDomain (): Observable<boolean> {
    if (!this.isMultiDomainSubject) {
      this.isMultiDomainSubject = new AsyncSubject<boolean>();
      this.http.get(DomainService.MULTI_TENANCY_URL).subscribe((res: Response) => {
        this.isMultiDomainSubject.next(res.json());
      }, (error: any) => {
        console.log('get isMultiDomain:' + error);
        this.isMultiDomainSubject.next(false);
      }, () => {
        this.isMultiDomainSubject.complete();
      });
    }
    return this.isMultiDomainSubject.asObservable();
  }

  getCurrentDomain (): Observable<Domain> {
    if (!this.domainSubject) {
      this.domainSubject = new BehaviorSubject<Domain>(null);
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

  getDomains (): Promise<Domain[]> {
    return this.http.get(DomainService.DOMAIN_LIST_URL).map((res: Response) => res.json()).toPromise();
  }

  setCurrentDomain (domain: Domain) {
    return this.http.put(DomainService.CURRENT_DOMAIN_URL, domain.code).toPromise().then(() => {
      if (this.domainSubject) {
        this.domainSubject.next(domain);
      }
    });
  }

  private getTitle (): Promise<string> {
    return this.http.get('rest/application/name').map((resp: Response) => resp.json()).toPromise();
  }

  setAppTitle () {
    this.getTitle().then((title) => {
      this.titleService.setTitle(title);
    });
  }

}
