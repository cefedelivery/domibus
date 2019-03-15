import {Injectable} from '@angular/core';
import {Headers, Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {User} from './user';
import {ReplaySubject} from 'rxjs';
import {SecurityEventService} from './security.event.service';
import {DomainService} from './domain.service';
import {PasswordPolicyRO} from './passwordPolicyRO';
import {AlertService} from '../common/alert/alert.service';

@Injectable()
export class SecurityService {
  static ROLE_AP_ADMIN = 'ROLE_AP_ADMIN';
  static ROLE_DOMAIN_ADMIN = 'ROLE_ADMIN';
  static ROLE_USER = 'ROLE_USER';

  passwordPolicy: Promise<PasswordPolicyRO>;
  pluginPasswordPolicy: Promise<PasswordPolicyRO>;
  public password: string;

  constructor(private http: Http,
              private securityEventService: SecurityEventService,
              private alertService: AlertService,
              private domainService: DomainService) {
  }

  login(username: string, password: string) {
    this.domainService.resetDomain();

    const headers = new Headers({'Content-Type': 'application/json'});
    return this.http.post('rest/security/authentication',
      {
        username: username,
        password: password
      }).subscribe((response: Response) => {
        this.updateCurrentUser(response.json());

        this.domainService.setAppTitle();

        this.securityEventService.notifyLoginSuccessEvent(response);
      },
      (error: any) => {
        console.log('Login error');
        this.securityEventService.notifyLoginErrorEvent(error);
      });
  }

  /**
   * It simulates the login function for an external authentication provider
   * Saves current user to local storage, etc
   */
  login_extauthprovider(): Promise<boolean> {
    console.log('login from auth external provider');
    return new Promise((resolve, reject) => {
      const res = this.getCurrentUserAndSaveLocally();
      resolve(res);
    });
  }

  async getCurrentUserAndSaveLocally() {
    let userSet = false;
    try {
      //get the user from server and write it in local storage
      const user = await this.getCurrentUserFromServer();
      if (user) {
        this.updateCurrentUser(user);
        //this.domainService.setAppTitle();
        userSet = true;
      }
    } catch (ex) {
      console.log('getCurrentUserAndSaveLocally error' + ex);
    }
    return userSet;
  }

  logout() {
    console.log('security service - logout');
    this.alertService.close();

    this.clearSession();

    this.http.delete('rest/security/authentication').subscribe((res: Response) => {
        this.securityEventService.notifyLogoutSuccessEvent(res);
      },
      (error: any) => {
        this.securityEventService.notifyLogoutErrorEvent(error);
      });
  }

  getPluginPasswordPolicy(): Promise<PasswordPolicyRO> {
    if (!this.pluginPasswordPolicy) {
      this.pluginPasswordPolicy = this.http.get('rest/application/pluginPasswordPolicy')
        .map(this.extractData)
        .map(this.formatValidationMessage)
        .catch(err => this.alertService.handleError(err))
        .toPromise();
    }
    return this.pluginPasswordPolicy;
  }

  private formatValidationMessage(policy: PasswordPolicyRO) {
    policy.validationMessage = policy.validationMessage.split(';').map(el => '- ' + el + '<br>').join('');
    return policy;
  }

  clearSession() {
    this.domainService.resetDomain();
    sessionStorage.removeItem('currentUser');
  }

  getCurrentUser(): User {
    const storedUser = sessionStorage.getItem('currentUser');
    return storedUser ? JSON.parse(storedUser) : null;
  }

  updateCurrentUser(user: User): void {
    console.log('save current user on local storage');
    sessionStorage.setItem('currentUser', JSON.stringify(user));
  }

  private getCurrentUsernameFromServer(): Observable<string> {
    const subject = new ReplaySubject();
    this.http.get('rest/security/username')
      .subscribe((res: Response) => {
        subject.next(res.text());
      }, (error: any) => {
        subject.next(null);
      });
    return subject.asObservable();
  }

  getCurrentUserFromServer(): Promise<User> {
    console.log('getCurrentUserFromServer');
    return this.http.get('rest/security/user').
      map((res: Response) => res.json()).toPromise();
  }


  isAuthenticated(callServer: boolean = false): Observable<boolean> {
    const subject = new ReplaySubject();
    if (callServer) {
      // we get the username from the server to trigger the redirection to the login screen in case the user is not authenticated
      this.getCurrentUsernameFromServer()
        .subscribe((user: string) => {
          let userUndefined = (user == null || user == "");
          subject.next(!userUndefined);
        }, (error: any) => {
          console.log('isAuthenticated error' + error);
          subject.next(false);
        });

    } else {
      const currentUser = this.getCurrentUser();
      subject.next(currentUser !== null);
    }
    return subject.asObservable();
  }

  isCurrentUserSuperAdmin(): boolean {
    return this.isCurrentUserInRole([SecurityService.ROLE_AP_ADMIN]);
  }

  isCurrentUserAdmin(): boolean {
    return this.isCurrentUserInRole([SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN]);
  }

  hasCurrentUserPrivilegeUser(): boolean {
    return this.isCurrentUserInRole([SecurityService.ROLE_USER, SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN]);
  }

  isUserFromExternalAuthProvider(): boolean {
    const user = this.getCurrentUser();
    return user ? user.externalAuthProvider : false;
  }

  isCurrentUserInRole(roles: Array<string>): boolean {
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

  isAuthorized(roles: Array<string>): Observable<boolean> {
    const subject = new ReplaySubject();

    this.isAuthenticated(false).subscribe((isAuthenticated: boolean) => {
      console.log('isAuthorized -> isAuthenticated:' + isAuthenticated);
      if (isAuthenticated && roles) {
        const hasRole = this.isCurrentUserInRole(roles);
        console.log('isAuthorized - hasRole:' + hasRole);
        subject.next(hasRole);
      } else {
        console.log('isAuthorized - not');
        subject.next(false);
      }
    });
    return subject.asObservable();
  }

  getPasswordPolicy(): Promise<PasswordPolicyRO> {
    if (!this.passwordPolicy) {
      this.passwordPolicy = this.http.get('rest/application/passwordPolicy')
        .map(this.extractData)
        .map(this.formatValidationMessage)
        .catch(err => this.alertService.handleError(err))
        .toPromise();
    }
    return this.passwordPolicy;
  }

  mustChangePassword(): boolean {
    return this.isDefaultPasswordUsed();
  }

  private isDefaultPasswordUsed(): boolean {
    const currentUser: User = this.getCurrentUser();
    return currentUser && currentUser.defaultPasswordUsed;
  }

  shouldChangePassword(): any {
    if (this.isDefaultPasswordUsed()) {
      return {
        response: true,
        reason: 'You are using the default password. Please change it now in order to be able to use the console.',
        redirectUrl: 'changePassword'
      };
    }

    const currentUser = this.getCurrentUser();
    if (currentUser && currentUser.daysTillExpiration !== null) {
      let interval: string = 'in ' + currentUser.daysTillExpiration + ' day(s)';
      if (currentUser.daysTillExpiration === 0) {
        interval = 'today';
      }
      return {
        response: true,
        reason: 'The password is about to expire ' + interval + '. We recommend changing it.',
        redirectUrl: 'changePassword'
      };
    }
    return {response: false};

  }

  private extractData(res: Response) {
    const result = res.json() || {};
    return result;
  }

  async changePassword(params): Promise<any> {
    const res = this.http.put('rest/security/user/password', params).toPromise();
    await res;

    const currentUser = this.getCurrentUser();
    currentUser.defaultPasswordUsed = false;
    this.updateCurrentUser(currentUser);

    return res;
  }

}

