import {UserResponseRO} from './user';
import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {AlertService} from '../alert/alert.service';
import {Observable} from 'rxjs/Observable';
import {SecurityService} from '../security/security.service';
import {DomainService} from '../security/domain.service';

@Injectable()
export class UserService {

  constructor (private http: Http,
               private alertService: AlertService,
               private securityService: SecurityService,
               private domainService: DomainService) {
  }

  getUsers (filter: UserSearchCriteria): Observable<UserResponseRO[]> {
    return this.http.get('rest/user/users')
      .map(this.extractData)
      .filter(this.filterData(filter))
      .catch(err => this.alertService.handleError(err));
  }

  getUserNames (): Observable<string> {
    return this.http.get('rest/user/users')
      .flatMap(res => res.json())
      .map((user: UserResponseRO) => user.userName)
      .catch(err => this.alertService.handleError(err));
  }

  getUserRoles (): Observable<String[]> {
    return this.http.get('rest/user/userroles')
      .map(this.extractData)
      .catch(err => this.alertService.handleError(err));
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

  async isDomainVisible (): Promise<boolean> {
    const isMultiDomain = await this.domainService.isMultiDomain().toPromise();
    return isMultiDomain && this.securityService.isCurrentUserSuperAdmin();
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

  passwordPolicy: Promise<PasswordPolicyRO>;

  getPasswordPolicy (): Promise<PasswordPolicyRO> {
    if (!this.passwordPolicy) {
      this.passwordPolicy = this.http.get('rest/application/passwordPolicy')
        .map(this.extractData)
        .map((policy: PasswordPolicyRO) => {
          policy.validationMessage = policy.validationMessage.split(';').map(el => '- ' + el + '<br>').join('');
          return policy;
        })
        .catch(err => this.alertService.handleError(err))
        .toPromise();
    }
    return this.passwordPolicy;
  }
}

export class UserSearchCriteria {
  authRole: string;
  userName: string;
  deleted: boolean;
}

export class PasswordPolicyRO {
  pattern: string;
  validationMessage: string;
}
