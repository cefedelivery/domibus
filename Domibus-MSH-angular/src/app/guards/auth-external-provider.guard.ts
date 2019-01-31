import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {SecurityService} from '../security/security.service';

/**
 * It will redirect to home ('/') if the external provider = true
 */
@Injectable()
export class AuthExternalProviderGuard implements CanActivate {

  constructor(private router: Router, private securityService: SecurityService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const isUserFromExternalAuthProvider = this.securityService.isUserFromExternalAuthProvider();

    if (isUserFromExternalAuthProvider) {
      console.log('redirect to /');
      this.router.navigate(['/']);
      return false;
    }
    return true;
  }
}
