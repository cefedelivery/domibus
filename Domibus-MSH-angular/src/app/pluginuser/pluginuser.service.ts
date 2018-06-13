import {Injectable} from '@angular/core';
import {Http, URLSearchParams, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {PluginUserRO} from './pluginuser';
import {UserState} from '../user/user';
import {UserService} from '../user/user.service';
import {SecurityService} from '../security/security.service';

@Injectable()
export class PluginUserService {

  static readonly PLUGIN_USERS_URL: string = 'rest/plugin/users';
  public static passwordPattern = '^(?=.*[A-Z])(?=.*[ !#$%&\'()*+,-./:;<=>?@\\[^_`{|}~\\\]"])(?=.*[0-9])(?=.*[a-z]).{8,32}$';

  readonly ROLE_AP_ADMIN = SecurityService.ROLE_AP_ADMIN;

  constructor (private http: Http, private userService: UserService) {
  }

  getUsers (filter?: PluginUserSearchCriteria)
    : Observable<{ entries: PluginUserRO[], count: number }> {
    const searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('page', '0');
    searchParams.set('pageSize', '10');
    searchParams.set('orderBy', 'entityId');
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
    // console.log('filter : ', JSON.stringify(filter))
    // console.log('searchParams : ', JSON.stringify(searchParams), searchParams)

    return this.http.get(PluginUserService.PLUGIN_USERS_URL, {search: searchParams})
      .map(this.extractData)
      .catch(this.handleError);
  }

  createNew (): PluginUserRO {
    const item = new PluginUserRO();
    item.status = UserState[UserState.NEW];
    item.username = ' ';
    return item;

  }

  saveUsers (users: PluginUserRO[]): Promise<Response> {
    return this.http.put(PluginUserService.PLUGIN_USERS_URL, users).toPromise();
  }


  getUserRoles (): Observable<String[]> {
    return this.userService.getUserRoles().map(items => items.filter(item => item !== this.ROLE_AP_ADMIN));
  }

  private extractData (res: Response) {
    const body = res.json();
    return body || {};
  }

  private handleError (error: Response | any) {
    let errMsg: string;
    if (error instanceof Response) {
      const body = error.json() || '';
      const err = body.error || JSON.stringify(body);
      errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    // console.error(errMsg);
    return Promise.reject(errMsg);
  }
}

export class PluginUserSearchCriteria {
  authType: string;
  authRole: string;
  userName: string;
  originalUser: string;
}
