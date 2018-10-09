import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import {MdDialog} from '@angular/material';
import {SecurityService} from './security.service';

@Injectable()
export class DefaultPasswordGuard implements CanActivate, CanDeactivate<any> {

  constructor (public dialog: MdDialog, private securityService: SecurityService) {
  };

  canActivate (next: ActivatedRouteSnapshot,
               state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {

    return !this.securityService.shouldChangePassword();
    // return new Promise((resolve, reject) => {
    //   this.securityService.shouldChangePassword().then((res) => {
    //     resolve(!res);
    //   });
    // });

  }

  canDeactivate (component: any, currentRoute: ActivatedRouteSnapshot,
                 currentState: RouterStateSnapshot,
                 nextState?: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {

    return true;

  }

}
