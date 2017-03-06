import {Injectable} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import {SecurityService} from "../security/security.service";
import {IsAuthorized} from "../security/is-authorized.directive";
import {ReplaySubject} from "rxjs";

@Injectable()
export class AuthorizedGuard implements CanActivate {

  constructor(private router: Router, private securityService: SecurityService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    console.debug("AuthorizedGuard");
    let allowedRoles = route.data["allowedRoles"] as Array<string>;
    let subject = new ReplaySubject();
    this.securityService.isAuthorized(allowedRoles).subscribe((isAuthorized:boolean) => {
      console.debug("AuthorizedGuard canActivate [" + isAuthorized + "]");
      subject.next(isAuthorized);
    },(error:any) => {
      console.debug("AuthorizedGuard canActivate error [" + error + "]");
    });
    return subject.asObservable();
  }
}
