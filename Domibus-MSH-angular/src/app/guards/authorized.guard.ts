import {Injectable} from "@angular/core";
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from "@angular/router";
import {SecurityService} from "../security/security.service";
import {ReplaySubject} from "rxjs";

@Injectable()
export class AuthorizedGuard implements CanActivate {

  constructor(private securityService: SecurityService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    console.debug("AuthorizedGuard");
    let allowedRoles = this.getAllowedRoles(route);
    let subject = new ReplaySubject();
    this.securityService.isAuthorized(allowedRoles).subscribe((isAuthorized:boolean) => {
      console.debug("AuthorizedGuard canActivate [" + isAuthorized + "]");
      subject.next(isAuthorized);
    },(error:any) => {
      console.debug("AuthorizedGuard canActivate error [" + error + "]");
    });
    return subject.asObservable();
  }

  getAllowedRoles(route: ActivatedRouteSnapshot) {
    return route.data["allowedRoles"] as Array<string>;
  }
}
