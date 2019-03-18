import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {SecurityService} from '../../security/security.service';
import {DomibusInfoService} from "../appinfo/domibusinfo.service";

@Injectable()
export class AuthenticatedGuard implements CanActivate {

  constructor (private router: Router, private securityService: SecurityService, private domibusInfoService: DomibusInfoService) {
  }

  // canActivate (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
  //   const subject = new ReplaySubject();
  //   this.securityService.isAuthenticated(true).subscribe((isAuthenticated: boolean) => {
  //     if (isAuthenticated) {
  //       subject.next(true);
  //     } else {
  //       // not logged in so redirect to login page with the return url
  //       // console.log('AuthenticatedGuard: not logged in -> redirect to login');
  //
  //       // todo: the call to clear is not cohesive, should refactor
  //       this.securityService.clearSession();
  //       // todo: the redirect is duplicated, should refactor
  //       this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
  //       subject.next(false);
  //     }
  //   });
  //   return subject.asObservable();
  // }


  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    let canActivate = false;
    const isAuthenticated = await this.securityService.isAuthenticated(true);
    console.log('AuthenticatedGuard isAuthenticatedFromServer=' + isAuthenticated);



    if (isAuthenticated) {
      canActivate = true;
      const isUserFromExternalAuthProvider = this.securityService.isUserFromExternalAuthProvider();
      console.log('isUserFromExternalAuthProvider='+isUserFromExternalAuthProvider);

      //check also authorization
      const allowedRoles = route.data.checkRoles;
      console.log('going to check authorization');
      if (!!allowedRoles) { //only if there are roles to check
        console.log('going to check authorization for: ' + allowedRoles);
        const isAuthorized = this.securityService.isAuthorized(allowedRoles);
        if (!isAuthorized) {
          canActivate = false;

          this.router.navigate([isUserFromExternalAuthProvider ? '/notAuthorized': '/']);
        }
      }
    } else {
      // not logged in so redirect to login page with the return url
      // console.log('AuthenticatedGuard: not logged in -> redirect to login');

      // todo: the call to clear is not cohesive, should refactor
      this.securityService.clearSession();
      // todo: the redirect is duplicated, should refactor
      this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
    }
    console.log('AuthenticatedGuard canActivate: ' + canActivate);
    return canActivate;
  }

}
