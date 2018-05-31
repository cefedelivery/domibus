import {Injectable} from "@angular/core";
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from "@angular/router";
import {SecurityService} from "../security/security.service";
import {ReplaySubject} from "rxjs";

@Injectable()
export class AuthenticatedGuard implements CanActivate {

  constructor(private router: Router, private securityService: SecurityService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const subject = new ReplaySubject();
    this.securityService.isAuthenticated(true).subscribe((isAuthenticated: boolean) => {
      if(isAuthenticated) {
        subject.next(true);
      } else {
        // not logged in so redirect to login page with the return url
        this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
        subject.next(false);
      }
    });
    return subject.asObservable()
  }
}
