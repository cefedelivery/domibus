import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, RouterStateSnapshot} from "@angular/router";
import {SecurityService} from "../security/security.service";
import {AuthorizedGuard} from "./authorized.guard";

@Injectable()
export class AuthorizedAdminGuard extends AuthorizedGuard {

  constructor(securityService: SecurityService) {
    super(securityService);
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return super.canActivate(route, state);
  }


  getAllowedRoles(route: ActivatedRouteSnapshot): Array<string> {
    return [SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN];
  }
}
