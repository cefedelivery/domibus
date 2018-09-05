import {Injectable} from '@angular/core';
import {NavigationEnd, NavigationStart, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {Subject} from 'rxjs/Subject';
import {Response} from '@angular/http';

@Injectable()
export class AlertService {
  private subject = new Subject<any>();
  private previousRoute: string;


  //TODO move the logic in the ngInit block
  constructor (private router: Router) {
    this.previousRoute = '';
    // clear alert message on route change
    router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        if (this.isRouteChanged(event.url)) {
          console.log('Clearing alert when navigating from [' + this.previousRoute + '] to [' + event.url + ']');
          this.clearAlert();
        } else {
          console.log('Alert kept when navigating from [' + this.previousRoute + '] to [' + event.url + ']');
        }
      } else if (event instanceof NavigationEnd) {
        let navigationEnd: NavigationEnd = event;
        this.previousRoute = navigationEnd.url;
      }
    });
  }

  getPath (url: string): string {
    var parser = document.createElement('a');
    parser.href = url;
    return parser.pathname;
  }

  isRouteChanged (currentRoute: string): boolean {
    let result = false;
    const previousRoutePath = this.getPath(this.previousRoute);
    const currentRoutePath = this.getPath(currentRoute);
    if (previousRoutePath !== currentRoutePath) {
      result = true;
    }
    return result;
  }

  clearAlert (): void {
    this.subject.next();
  }

  success (message: string, keepAfterNavigationChange = false) {
    this.subject.next({type: 'success', text: message});
  }

  error (message: string | any, keepAfterNavigationChange = false, fadeTime: number = 0) {
    if (message.handled) return;

    const errMsg = this.formatError(message);
    this.subject.next({type: 'error', text: errMsg});
    if (fadeTime) {
      setTimeout(() => this.clearAlert(), fadeTime);
    }
  }

  exception (message: string, error: any, keepAfterNavigationChange = false, fadeTime: number = 0) {
    const errMsg = this.formatError(error, message);
    this.error(errMsg, keepAfterNavigationChange, fadeTime);
  }

  getMessage (): Observable<any> {
    return this.subject.asObservable();
  }

  private formatError (error: Response | string | any, message: string = null): string {
    let errMsg: string = typeof error === 'string' ? error : error.message;
    if (!errMsg) {
      try {
        if (error.headers && error.headers.get('content-type') !== 'text/html;charset=utf-8') {
          errMsg = error.json ? (error.json().message || error.json() || error) : (error._body || error);
        } else {
          errMsg = error._body ? error._body.match(/<h1>(.+)<\/h1>/)[1] : error;
        }
      } catch (e) {
      }
    }
    return (message ? message + ' \n' : '') + (errMsg || '');
  }

  handleError (error: Response | any) {

    this.error(error, false);

    let errMsg: string;
    if (error instanceof Response) {
      const body = error.json() || '';
      const err = body.error || JSON.stringify(body);
      errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    console.error(errMsg);
    return Promise.reject({reason: errMsg, handled: true});
  }
}
