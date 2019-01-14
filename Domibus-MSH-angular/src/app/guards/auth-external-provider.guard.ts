import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {SecurityService} from '../security/security.service';
import {ReplaySubject} from 'rxjs';

@Injectable()
export class AuthExternalProviderGuard implements CanActivate {

  constructor (private router: Router, private securityService: SecurityService) {
  }

  canActivate (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const subject = new ReplaySubject();
    const isUserFromExternalAuthProvider = this.securityService.isUserFromExternalAuthProvider();

    if (isUserFromExternalAuthProvider) {
      this.router.navigate(['/']);
      subject.next(false);
    } else {
      subject.next(true);
    }

    return subject.asObservable();
  }
}
