import {Injectable} from '@angular/core';
import {Http, URLSearchParams, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {PluginUserRO} from './pluginuser';
import {UserState} from '../user/user';
import {UserService} from '../user/user.service';
import {SecurityService} from '../security/security.service';
import {UserComponent} from '../user/user.component';
import {AlertService} from '../alert/alert.service';

@Injectable()
export class PluginUserService {

  static readonly PLUGIN_USERS_URL: string = 'rest/plugin/users';

  public static passwordPattern = '^(?=.*[A-Z])(?=.*[ !#$%&\'()*+,-./:;<=>?@\\[^_`{|}~\\\]"])(?=.*[0-9])(?=.*[a-z]).{8,32}$';

  public static originalUserPattern = 'urn:oasis:names:tc:ebcore:partyid\\-type:[a-zA-Z0-9_:-]+:[a-zA-Z0-9_:-]+';
  public static originalUserMessage = 'You should follow the rule: urn:oasis:names:tc:ebcore:partyid-type:[unregistered]:[corner]';

  public static certificateIdPattern = 'CN=[a-zA-Z0-9_]+,O=[a-zA-Z0-9_]+,C=[a-zA-Z]{2}:[a-zA-Z0-9]+';
  public static certificateIdMessage = 'You should follow the rule CN=[name],O=[name],C=[country code]:[id]';

  public static CSV_URL = 'rest/plugin/csv';

  readonly ROLE_AP_ADMIN = SecurityService.ROLE_AP_ADMIN;

  constructor (private http: Http, private userService: UserService, private alertService: AlertService) {
  }

  getUsers (filter?: PluginUserSearchCriteria)
    : Observable<{ entries: PluginUserRO[], count: number }> {

    const searchParams = this.createFilterParams(filter);

    return this.http.get(PluginUserService.PLUGIN_USERS_URL, {search: searchParams})
      .map(this.extractData)
      .catch(err => this.alertService.handleError(err));
  }

  createFilterParams (filter: PluginUserSearchCriteria) {
    const searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('page', '0');
    searchParams.set('pageSize', '10000');

    if (filter.authType) {
      searchParams.set('authType', filter.authType);
    }
    if (filter.authRole) {
      searchParams.set('authRole', filter.authRole);
    }
    if (filter.userName) {
      searchParams.set('userName', filter.userName);
    }
    if (filter.originalUser) {
      searchParams.set('originalUser', filter.originalUser);
    }
    return searchParams;
  }

  createNew (): PluginUserRO {
    const item = new PluginUserRO();
    item.status = UserState[UserState.NEW];
    item.username = '';
    return item;
  }

  saveUsers (users: PluginUserRO[]): Promise<Response> {
    users = users.filter(el => el.status !== UserState[UserState.PERSISTED]);
    return this.http.put(PluginUserService.PLUGIN_USERS_URL, users).toPromise();
  }


  getUserRoles (): Observable<String[]> {
    return this.userService.getUserRoles().map(items => items.filter(item => item !== this.ROLE_AP_ADMIN));
  }

  private extractData (res: Response) {
    const body = res.json();
    const r = body || {};
    r.entries.forEach(el => el.hiddenPassword = '******');
    return r;
  }

}

export class PluginUserSearchCriteria {
  authType: string;
  authRole: string;
  userName: string;
  originalUser: string;
}
