import {Injectable} from '@angular/core';
import {Router, NavigationStart, NavigationEnd} from '@angular/router';
import {Observable} from 'rxjs';
import {Subject} from 'rxjs/Subject';

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
    let previousRoutePath = this.getPath(this.previousRoute);
    let currentRoutePath = this.getPath(currentRoute);
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

  error (message: string, keepAfterNavigationChange = false) {
    this.subject.next({type: 'error', text: message});
  }

  exception (message: string, error: any, keepAfterNavigationChange = false): void {
    const errMsg = error.message || (error.json ? error.json().message : error );
    this.error(message + ' \n' + errMsg, keepAfterNavigationChange);
  }

  getMessage (): Observable<any> {
    return this.subject.asObservable();
  }
}
