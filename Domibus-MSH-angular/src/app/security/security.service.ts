import {Injectable} from '@angular/core';
import {Headers, Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {User} from './user';
import {ReplaySubject} from 'rxjs';
import {SecurityEventService} from './security.event.service';
import {DomainService} from './domain.service';
import {PasswordPolicyRO} from './passwordPolicyRO';
import {AlertService} from '../alert/alert.service';

@Injectable()
export class SecurityService {
  static ROLE_AP_ADMIN = 'ROLE_AP_ADMIN';
  static ROLE_DOMAIN_ADMIN = 'ROLE_ADMIN';

  constructor (private http: Http,
               private securityEventService: SecurityEventService,
               private alertService: AlertService,
               private domainService: DomainService) {
  }

  login (username: string, password: string) {
    this.domainService.resetDomain();

    const headers = new Headers({'Content-Type': 'application/json'});
    return this.http.post('rest/security/authentication',
      {
        username: username,
        password: password
      }).subscribe((response: Response) => {
        localStorage.setItem('currentUser', JSON.stringify(response.json()));

        this.domainService.setAppTitle();

        this.securityEventService.notifyLoginSuccessEvent(response);
      },
      (error: any) => {
        console.log('Login error');
        this.securityEventService.notifyLoginErrorEvent(error);
      });
  }

  logout () {
    this.clearSession();

    this.http.delete('rest/security/authentication').subscribe((res: Response) => {
        this.securityEventService.notifyLogoutSuccessEvent(res);
      },
      (error: any) => {
        this.securityEventService.notifyLogoutErrorEvent(error);
      });
  }

  clearSession () {
    this.domainService.resetDomain();
    localStorage.removeItem('currentUser');
  }

  getCurrentUser (): User {
    let storedUser = localStorage.getItem('currentUser');
    return storedUser ? JSON.parse(storedUser) : null;
  }

  private getCurrentUsernameFromServer (): Observable<string> {
    let subject = new ReplaySubject();
    this.http.get('rest/security/user')
      .subscribe((res: Response) => {
        subject.next(res.text());
      }, (error: any) => {
        //console.log('getCurrentUsernameFromServer:' + error);
        subject.next(null);
      });
    return subject.asObservable();
  }

  isAuthenticated (callServer: boolean = false): Observable<boolean> {
    let subject = new ReplaySubject();
    if (callServer) {
      //we get the username from the server to trigger the redirection to the login screen in case the user is not authenticated
      this.getCurrentUsernameFromServer()
        .subscribe((user: string) => {
          // console.log('isAuthenticated: getCurrentUsernameFromServer [' + user + ']');
          subject.next(user !== null);
        }, (user: string) => {
          console.log('isAuthenticated error' + user);
          subject.next(false);
        });

    } else {
      let currentUser = this.getCurrentUser();
      subject.next(currentUser !== null);
    }
    return subject.asObservable();
  }

  isCurrentUserSuperAdmin (): boolean {
    return this.isCurrentUserInRole([SecurityService.ROLE_AP_ADMIN]);
  }

  isCurrentUserAdmin (): boolean {
    return this.isCurrentUserInRole([SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN]);
  }

  isCurrentUserInRole (roles: Array<string>): boolean {
    let hasRole = false;
    const currentUser = this.getCurrentUser();
    if (currentUser && currentUser.authorities) {
      roles.forEach((role: string) => {
        if (currentUser.authorities.indexOf(role) !== -1) {
          hasRole = true;
        }
      });
    }
    return hasRole;
  }

  isAuthorized (roles: Array<string>): Observable<boolean> {
    let subject = new ReplaySubject();

    this.isAuthenticated(false).subscribe((isAuthenticated: boolean) => {
      if (isAuthenticated && roles) {
        let hasRole = this.isCurrentUserInRole(roles);
        subject.next(hasRole);
      }
    });
    return subject.asObservable();
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

  mustChangePassword (): boolean {
    const currentUser: User = this.getCurrentUser();
    return currentUser.defaultPasswordUsed;
  }

  shouldChangePassword (): any {
    if (this.mustChangePassword())
      return {response: true, reason: 'The user has the default password. Please change it now in order to be able to use the console.'};

    const currentUser = this.getCurrentUser();
    if (currentUser.daysTillExpiration > 0)
      return {response: true, reason: 'The password is about to expire in ' + currentUser.daysTillExpiration + ' days. We recommend changing it.'};
    else
      return {response: false};
  }

  // async mustChangePassword (): Promise<boolean> {
  //   const currentUser: User = this.getCurrentUser();
  //   if (!currentUser.defaultPasswordUsed) {
  //     return Promise.resolve(false);
  //   }
  //
  //   const policy: PasswordPolicyRO = await this.getPasswordPolicy();
  //   return policy.checkDefault;
  // }

  private extractData (res: Response) {
    const result = res.json() || {};
    return result;
  }
}

