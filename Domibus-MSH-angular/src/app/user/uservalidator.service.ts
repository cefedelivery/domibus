import {UserResponseRO, UserState} from './user';
import {AlertService} from '../alert/alert.service';
import {Injectable} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {SecurityService} from '../security/security.service';

/**
 * Created by dussath on 6/20/17.
 */
@Injectable()
export class UserValidatorService {
  constructor (private alertService: AlertService,
  private securityService: SecurityService) {
  }

  validateUsers (users: UserResponseRO[]): boolean {
    
    let errorMessage: string = '';
    const modifiedUsers = users.filter(user=>user.status === UserState[UserState.UPDATED])
    if (modifiedUsers.length == 0) {
      return true;
    }
    errorMessage = errorMessage.concat(this.checkUserNameDuplication(users));
    errorMessage = errorMessage.concat(this.validateDomains(users));

    // for (let u in modifiedUsers) {
    //   let user: UserResponseRO = modifiedUsers[u];
    //   let number = users.indexOf(user) + 1;
    //   if (user.status === UserState[UserState.NEW]) {
    //     errorMessage = errorMessage.concat(this.validateNewUsers(user, number));
    //   }
    //   else if (user.status === UserState[UserState.UPDATED]) {
    //     errorMessage = errorMessage.concat(this.validateRoles(user, number));
    //     errorMessage = errorMessage.concat(this.validateEmail(user, number));
    //   }
    // }
        console.log('validateUsers',errorMessage);
    return this.triggerValidation(errorMessage);
  }

  validateDomains (users: UserResponseRO[]): string {
    let errorMessage: string = '';
    const activeUsers = users.filter(user => user.active);
    
    // check at least one active domain admin
    const domainAdmins = activeUsers.filter(user => user.roles.includes(SecurityService.ROLE_DOMAIN_ADMIN));
    if (domainAdmins.length < 1) {
      errorMessage = errorMessage.concat('There must always be at least one active Domain Admin for each Domain'); 
      //throw Error('There must always be at least one active Domain Admin for each Domain');
    }
    // check at least one ap admin
    if(this.securityService.isCurrentUserSuperAdmin()) {
      const apAdmins = activeUsers.filter(user => user.roles.includes(SecurityService.ROLE_AP_ADMIN));
      if (apAdmins.length < 1) {
        errorMessage = errorMessage.concat('There must always be at least one active AP Admin'); 
        //throw Error('There must always be at least one active AP Admin');
      }
    }
    return errorMessage;
  }
  
  validateNewUsers (user: UserResponseRO, number): string {
    let errorMessage: string = '';
    if (user.userName == null || user.userName.trim() === '') {
      errorMessage = errorMessage.concat('User ' + number + ' has no username defined\n');
    }
    errorMessage = errorMessage.concat(this.validateRoles(user, number));
    errorMessage = errorMessage.concat(this.validateEmail(user, number));
    if (user.password == null || user.password.trim() === '') {
      errorMessage = errorMessage.concat('User ' + number + ' has no password defined\n');
    }
    return errorMessage;
  }

  checkUserNameDuplication (allUsers: UserResponseRO[]) {
    let errorMessage: string = '';
    let seen = new Set();
    allUsers.every(function (user) {
      if (seen.size === seen.add(user.userName).size) {
        errorMessage = errorMessage.concat('Duplicate user name with user ' + allUsers.indexOf(user) + ' ');
        return false;
      }
      return true;
    });
    return errorMessage;
  }

  triggerValidation (errorMessage: string): boolean {
    if (errorMessage.trim()) {
      this.alertService.clearAlert();
      this.alertService.error(errorMessage);
      return false;
    }
    return true;
  }

  validateRoles (user: UserResponseRO, number): string {
    let errorMessage: string = '';
    if (user.roles == null || user.roles.trim() === '') {
      errorMessage = errorMessage.concat('User ' + number + ' has no role defined\n');
    }
    return errorMessage;
  }

  validateEmail (user: UserResponseRO, number): string {
    const email: string = user.email;
    const EMAIL_REGEXP = /^[a-z0-9!#$%&'*+\/=?^_`{|}~.-]+@[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)*$/i;

    if (email !== '' && (email.length <= 5 || !EMAIL_REGEXP.test(email))) {
      return 'incorrectMailFormat for user ' + number;
    }
    return '';
  }

  matchPassword (form: AbstractControl) {
    const password = form.get('password').value; // to get value in input tag
    const confirmPassword = form.get('confirmation').value; // to get value in input tag
    if (password !== confirmPassword) {
      form.get('confirmation').setErrors({confirmation: true})
    }
  }

  validateDomain (form: AbstractControl) {
    const domain: string = form.get('domain').value;
    if (!domain) {
      form.get('domain').setErrors({required: true})
    }
  }

  validateForm () {
    return (form: AbstractControl) => {
      this.matchPassword(form);
    };
  }

}
