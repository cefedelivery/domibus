import {Injectable} from '@angular/core';
import {Http, Headers, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {User} from './user';
import {Router} from '@angular/router';
import {ReplaySubject} from 'rxjs';
import {SecurityEventService} from './security.event.service';

@Injectable()
export class SecurityService {
  public static ROLE_AP_ADMIN = 'ROLE_AP_ADMIN';

  constructor (private http: Http, private router: Router, private securityEventService: SecurityEventService) {
  }

  login (username: string, password: string) {
    const headers = new Headers({'Content-Type': 'application/json'});
    return this.http.post('rest/security/authentication',
      JSON.stringify({
        username: username,
        password: password
      }),
      {headers: headers})
      .subscribe((response: Response) => {
          const userInfo = response.json();

          // temporary mock- TODO
          userInfo.domain = 'MyDomain1';

          console.log('Login success', userInfo);
          localStorage.setItem('currentUser', JSON.stringify(userInfo));
          this.securityEventService.notifyLoginSuccessEvent(response);
        },
        (error: any) => {
          console.log('Login error', error);
          this.securityEventService.notifyLoginErrorEvent(error);
        });
  }

  logout () {
    console.log('Logging out');
    this.http.delete('rest/security/authentication').subscribe((res: Response) => {
        localStorage.removeItem('currentUser');
        this.securityEventService.notifyLogoutSuccessEvent(res);
      },
      (error: any) => {
        console.debug('error logging out [' + error + ']');
        this.securityEventService.notifyLogoutErrorEvent(error);
      });
  }

  getCurrentUser (): User {
    return JSON.parse(localStorage.getItem('currentUser'));
  }

  getCurrentUsernameFromServer (): Observable<string> {
    let subject = new ReplaySubject();
    this.http.get('rest/security/user')
      .subscribe((res: Response) => {
        subject.next(res.text());
      }, (error: any) => {
        console.log('getCurrentUsernameFromServer:' + error);
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
          console.log('isAuthenticated: getCurrentUsernameFromServer [' + user + ']');
          subject.next(user !== null);
        }, (user: string) => {
          console.log('isAuthenticated error' + user);
          subject.next(false);
        });

    } else {
      const currentUser = this.getCurrentUser();
      subject.next(currentUser !== null);
    }
    return subject.asObservable();
  }

  isCurrentUserAdmin (): boolean {
    return this.isCurrentUserInRole(['ROLE_ADMIN']);
  }

  isCurrentUserSuperAdmin (): boolean {
    //return true;
    return this.isCurrentUserInRole([SecurityService.ROLE_AP_ADMIN]);
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
}
