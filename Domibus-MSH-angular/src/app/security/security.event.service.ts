import {Injectable} from "@angular/core";
import {Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import "rxjs/add/operator/map";
import {Subject} from "rxjs";

@Injectable()
export class SecurityEventService {

  private loginSuccessSubject = new Subject<any>();
  private loginErrorSubject = new Subject<any>();
  private logoutSuccessSubject = new Subject<any>();
  private logoutErrorSubject = new Subject<any>();

  constructor() {
  }

  notifyLoginSuccessEvent(res: Response) {
    this.loginSuccessSubject.next(res);
  }

  onLoginSuccessEvent(): Observable<Response> {
    return this.loginSuccessSubject.asObservable();
  }

  notifyLoginErrorEvent(error: any) {
    this.loginErrorSubject.next(error);
  }

  onLoginErrorEvent(): Observable<any> {
    return this.loginErrorSubject.asObservable();
  }

  notifyLogoutSuccessEvent(res: Response) {
    this.logoutSuccessSubject.next(res);
  }

  onLogoutSuccessEvent(): Observable<Response> {
    return this.logoutSuccessSubject.asObservable();
  }

  notifyLogoutErrorEvent(error: any) {
    this.logoutErrorSubject.next(error);
  }

  onLogoutErrorEvent(): Observable<any> {
    return this.logoutErrorSubject.asObservable();
  }
}
