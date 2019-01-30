import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {SecurityService} from '../security/security.service';
import {ReplaySubject} from 'rxjs';

/**
 * It will redirect to home ('/') if the user is authenticated
 */
@Injectable()
export class RedirectHomeGuard implements CanActivate {

  constructor (private router: Router, private securityService: SecurityService) {
  }

  canActivate (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    console.log('RedirectHomeGuard canActivate');

    const subject = new ReplaySubject();
    this.securityService.isAuthenticated(true).subscribe((isAuthenticated: boolean) => {
      console.log('RedirectHomeGuard: isAuthenticated ' + isAuthenticated);
      if (isAuthenticated) {
        this.router.navigate(['/']);
        subject.next(false);

      } else {
        subject.next(true);
      }
    });

    return subject.asObservable();
  }
}
