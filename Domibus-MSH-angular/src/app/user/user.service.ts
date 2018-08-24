import {UserResponseRO} from './user';
import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {AlertService} from '../alert/alert.service';
import {Observable} from 'rxjs/Observable';
import {SecurityService} from '../security/security.service';
import {DomainService} from '../security/domain.service';

@Injectable()
export class UserService {
  isMultiDomain: boolean;

  constructor (private http: Http,
               private alertService: AlertService,
               private securityService: SecurityService,
               private domainService: DomainService) {
    this.domainService.isMultiDomain().subscribe((isMultiDomain: boolean) => {
      this.isMultiDomain = isMultiDomain;
    });
  }

  getUsers (filter: UserSearchCriteria): Observable<UserResponseRO[]> {
    return this.http.get('rest/user/users')
      .map(this.extractData)
      .filter(this.filterData(filter))
      .catch(this.handleError);
  }

  getUserNames (): Observable<string> {
    return this.http.get('rest/user/users')
      .flatMap(res => res.json())
      .map((user: UserResponseRO) => user.userName);
  }

  getUserRoles (): Observable<String[]> {
    return this.http.get('rest/user/userroles')
      .map(this.extractData)
      .catch(this.handleError);
  }

  deleteUsers (users: Array<UserResponseRO>): void {
    this.http.post('rest/user/delete', users).subscribe(res => {
      this.alertService.success('User(s) deleted', false);
    }, err => {
      this.alertService.error(err, false);
    });
  }

  saveUsers (users: Array<UserResponseRO>): void {
    this.http.post('rest/user/save', users).subscribe(res => {
      this.changeUserStatus(users);
      this.alertService.success('User saved', false);
    }, err => {
      this.alertService.error(err, false);
    });
  }

  changeUserStatus (users: Array<UserResponseRO>) {
    for (let u in users) {
      users[u].status = 'PERSISTED';
      users[u].password = '';
    }
  }

  isDomainVisible (): boolean {
    return this.isMultiDomain && this.securityService.isCurrentUserSuperAdmin();
  }

  private extractData (res: Response) {
    const result = res.json() || {};
    return result;
  }

  private filterData (filter: UserSearchCriteria) {
    return function (users) {
      let results = users.slice();
      if (filter.deleted != null) {
        results = users.filter(el => el.deleted === filter.deleted)
      }
      users.length = 0;
      users.push(...results);
      return users;
    }
  }

  private handleError (error: Response | any) {
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

export class UserSearchCriteria {
  authRole: string;
  userName: string;
  deleted: boolean;
}
