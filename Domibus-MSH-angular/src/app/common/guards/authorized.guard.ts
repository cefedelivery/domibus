import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {SecurityService} from "../../security/security.service";
import {ReplaySubject} from "rxjs";

@Injectable()
export class AuthorizedGuard implements CanActivate {

  constructor(private router: Router, private securityService: SecurityService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

    let allowedRoles = this.getAllowedRoles(route);
    let subject = new ReplaySubject();
    console.log('AuthorizedGuard allowedRoles:' + allowedRoles);
    this.securityService.isAuthorized(allowedRoles).subscribe((isAuthorized: boolean) => {
      console.log('AuthorizedGuard isAuthorized:' + isAuthorized);
      if (isAuthorized) {
        subject.next(true);
      } else {
        this.router.navigate(['/notAuthorized']);
        subject.next(false);
      }
    }, (error: any) => {
      console.log("AuthorizedGuard canActivate error [" + error + "]");
    });
    return subject.asObservable();
  }

  getAllowedRoles(route: ActivatedRouteSnapshot): Array<string> {
    return [SecurityService.ROLE_USER];
  }
}
