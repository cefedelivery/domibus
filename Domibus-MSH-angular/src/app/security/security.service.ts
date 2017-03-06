import {Injectable, OnInit} from '@angular/core';
import {Http, Headers, Response, RequestOptions} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {User} from './user';
import {Router} from "@angular/router";
import {HttpEventService} from "../http/http.event.service";
import {Subject, BehaviorSubject, ReplaySubject} from "rxjs";

@Injectable()
export class SecurityService {
  constructor(private http: Http, private router: Router, private httpEventService: HttpEventService) {
  }


//TODO rename to authenticate
  login(username: string, password: string) {
    let headers = new Headers({'Content-Type': 'application/json'});
    return this.http.post('rest/authenticate', JSON.stringify({username: username, password: password}), {headers: headers})
      //TODO use subscribe
      .map((response: Response) => {
        localStorage.setItem('currentUser', JSON.stringify(response.json()));
      });
  }

  testPost(username: string, password: string) {
    let headers = new Headers({'Content-Type': 'application/json'});
    return this.http.post('rest/testPost', JSON.stringify({username: username, password: password}), {headers: headers})
      .subscribe((res: Response) => {
        console.log("response1:" + res)
      }, (res: Response) => {
        console.log("response2:" + res)
      });
  }

  testGet() {
    let headers = new Headers({'Content-Type': 'application/json'});
    return this.http.get('rest/testGet', {headers: headers})
      .subscribe((res: Response) => {
        console.log("get response1:" + res)
      }, (res: Response) => {
        console.log("get response2:" + res)
      });
  }

  getCurrentUser(): User {
    return JSON.parse(localStorage.getItem('currentUser'));
  }

  getCurrentUsernameFromServer(): Observable<string> {
    let subject = new ReplaySubject();
    this.http.get('rest/user')
      .subscribe((res: Response) => {
        subject.next(res.text());
      }, (error: any) => {
        console.log("getCurrentUsernameFromServer:" + error);
        subject.next(null);
      });
    return subject.asObservable();
  }

  isAuthenticated(callServer: boolean = false): Observable<boolean> {
    let subject = new ReplaySubject();
    if (callServer) {
      //we get the username from the server to trigger the redirection to the login screen in case the user is not authenticated
      this.getCurrentUsernameFromServer()
        .subscribe((user: string) => {
          console.log("isAuthenticated: getCurrentUsernameFromServer [" + user + "]");
          subject.next(user !== null);
        }, (user: string) => {
          console.log("isAuthenticated error" + user);
          subject.next(false);
        });

    } else {
      let currentUser = this.getCurrentUser();
      subject.next(currentUser !== null);
    }
    return subject.asObservable();
  }

  isAuthorized(roles: Array<string>): Observable<boolean> {
    let subject = new ReplaySubject();

    this.isAuthenticated(false).subscribe((isAuthenticated: boolean) => {
      if (isAuthenticated && roles) {
        let currentUser = this.getCurrentUser();
        if (currentUser && currentUser.authorities) {
          let hasRole = false;
          roles.forEach((role: string) => {
            if (currentUser.authorities.indexOf(role) !== -1) {
              hasRole = true;
            }
          });
          subject.next(hasRole);
        } else {
          subject.next(false);
        }
      }
    });
    return subject.asObservable();
  }

  logout() {
    console.log("Logging out");
    this.http.get('rest/logout').subscribe((res: Response) => {
      localStorage.removeItem('currentUser');
      this.router.navigate(['/login']);
      }, (error: any) => {
        console.debug("error logging out [" + error + "]");
      });
  }
}
