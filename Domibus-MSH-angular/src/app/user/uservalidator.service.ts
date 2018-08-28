import {UserResponseRO, UserState} from './user';
import {AlertService} from '../alert/alert.service';
import {Injectable} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {SecurityService} from '../security/security.service';
import {ValidationErrors} from '../../../node_modules/@angular/forms/src/directives/validators';
import {DomainService} from '../security/domain.service';

/**
 * Created by dussath on 6/20/17.
 */
@Injectable()
export class UserValidatorService {
  constructor (private alertService: AlertService,
               private securityService: SecurityService,
               private domainService: DomainService) {
  }

  validateUsers (users: UserResponseRO[]): boolean {
    let errorMessage = '';
    errorMessage = errorMessage.concat(this.checkUserNameDuplication(users));
    errorMessage = errorMessage.concat(this.validateDomains(users));
    return this.triggerValidation(errorMessage);
  }

  validateDomains (users: UserResponseRO[]): string {
    let errorMessage = '';
    const activeUsers = users.filter(user => user.active);

    // check at least one active domain admin
    const domainAdmins = activeUsers.filter(user => user.roles.includes(SecurityService.ROLE_DOMAIN_ADMIN));
    if (domainAdmins.length < 1) {
      errorMessage = errorMessage.concat('There must always be at least one active Domain Admin for each Domain');
    }
    // check at least one ap admin
    if (this.securityService.isCurrentUserSuperAdmin()) {
      const apAdmins = activeUsers.filter(user => user.roles.includes(SecurityService.ROLE_AP_ADMIN));
      if (apAdmins.length < 1) {
        errorMessage = errorMessage.concat('There must always be at least one active AP Admin');
      }
    }
    return errorMessage;
  }

  private checkUserNameDuplication (allUsers: UserResponseRO[]) {
    let errorMessage = '';
    let seen = new Set();
    allUsers.every(function (user) {
      if (seen.size === seen.add(user.userName).size) {
        errorMessage = errorMessage.concat('Duplicate user name with user [' + allUsers.indexOf(user) + ']: ' + user.userName);
        return false;
      }
      return true;
    });
    return errorMessage;
  }

  private triggerValidation (errorMessage: string): boolean {
    if (errorMessage.trim()) {
      this.alertService.clearAlert();
      this.alertService.error(errorMessage);
      return false;
    }
    return true;
  }

  matchPassword (form: AbstractControl) {
    const password = form.get('password').value; // to get value in input tag
    const confirmPassword = form.get('confirmation').value; // to get value in input tag
    if (password !== confirmPassword) {
      form.get('confirmation').setErrors({confirmation: true})
    }
  }

  validateForm () {
    return (form: AbstractControl) => {
      this.matchPassword(form);
      this.validateDomainOnAdd(form);
    };
  }

  validateDomainOnAdd (form: AbstractControl) {
    const role = form.get('role').value;
    if (role && role !== SecurityService.ROLE_AP_ADMIN) {
      const domain = form.get('domain').value;
      this.domainService.getCurrentDomain().delay(0)
        .subscribe((currDomain) => {
          if (domain && currDomain && domain !== currDomain.code) {
            form.get('domain').setErrors({domain: true})
          }
        });
    }

  }

}
