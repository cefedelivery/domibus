import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {SecurityService} from "../../security/security.service";

@Injectable()
export class AuthorizedGuard implements CanActivate {

  constructor(private router: Router, private securityService: SecurityService/*,
              private domibusInfoService: DomibusInfoService*/) {
  }

  // canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
  //
  //   console.log('AuthorizedGuard - canActivate, route.url: '  + route.url);
  //   let allowedRoles = this.getAllowedRoles(route);
  //   let subject = new ReplaySubject();
  //   this.securityService.isAuthorized(allowedRoles).subscribe((isAuthorized: boolean) => {
  //     console.log('AuthorizedGuard - canActivate - route.url: ' + route.url);
  //     if (isAuthorized) {
  //       console.log('AuthorizedGuard - isAuthorized');
  //       subject.next(true);
  //     } else {
  //       this.router.navigate(['/notAuthorized']);
  //       subject.next(false);
  //     }
  //   }, (error: any) => {
  //     console.log("AuthorizedGuard canActivate error [" + error + "]");
  //   });
  //   return subject.asObservable();
  // }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

    console.log('AuthorizedGuard - canActivate, route.url: ' + route.url);
    const allowedRoles = this.getAllowedRoles(route);
    let canActivate = false;
    const isAuthorized = this.securityService.isAuthorized(allowedRoles);
    if (isAuthorized) {
      console.log('AuthorizedGuard - isAuthorized=' + isAuthorized);
      canActivate = true;
    } else {
      //if (this.domibusInfoService.isExtAuthProviderEnabled()) {
      this.router.navigate(['/notAuthorized']);
      //}
    }
    console.log('AuthorizedGuard - canActivate: ' + canActivate);
    return canActivate;
  }

  getAllowedRoles(route: ActivatedRouteSnapshot): Array<string> {
    return [SecurityService.ROLE_USER, SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN];
  }
}
