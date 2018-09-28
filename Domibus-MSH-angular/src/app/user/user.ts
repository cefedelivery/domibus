import {Domain} from '../security/domain';

export enum UserState {
  PERSISTED,
  NEW,
  UPDATED,
  REMOVED
}

export class UserResponseRO {
  userName: string;
  email: string;
  password: string;
  active: boolean;
  authorities: Array<string>;
  roles: string = '';
  domain: string = null;
  domainName: string = null;
  status: string;
  suspended: boolean;
  deleted: boolean;

  constructor (userName: string, domain: Domain, email: string, password: string, active: boolean, status: string,
               authorities: Array<string>, suspended: boolean, deleted: boolean) {
    this.userName = userName;
    this.email = email;
    this.password = password;
    this.status = status;
    this.active = active;
    this.suspended = suspended;
    this.authorities = authorities;
    for (let authority in authorities) {
      this.roles = this.roles.concat(authorities[authority]).concat(' ');
    }
    this.domain = domain ? domain.code : null;
    this.deleted = deleted;
    this.domainName = domain ? domain.name : null;
  }
}
