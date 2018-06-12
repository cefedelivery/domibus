import {Injectable} from '@angular/core';
import {Http, URLSearchParams, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {PluginUserRO} from './pluginuser';
import {UserState} from '../user/user';

@Injectable()
export class PluginUserService {

  static readonly PLUGIN_USERS_URL: string = 'rest/plugin/users';

  constructor (private http: Http) {
  }

  getUsers (filter?: PluginUserSearchCriteria): Observable<{ entries: PluginUserRO[], count: number }> {
    let searchParams: URLSearchParams = new URLSearchParams();
    // searchParams.set('page', offset.toString());
    // searchParams.set('pageSize', pageSize.toString());
    // searchParams.set('orderBy', orderBy);
    searchParams.set('page', '0');
    searchParams.set('pageSize', '10');
    searchParams.set('orderBy', 'entityId');
    if (filter.authType) {
      console.log(' filter.authType ', filter.authType)
      searchParams.set('authType', filter.authType);
    }
    if (filter.authRole) {
      console.log(' filter.authRole ', filter.authRole)
      searchParams.set('authRole', filter.authRole);
    }
    if (filter.userName) {
      console.log(' filter.userName ', filter.userName)
      searchParams.set('userName', filter.userName);
    }
    if (filter.originalUser) {
      console.log(' filter.originalUser ', filter.originalUser)
      searchParams.set('originalUser', filter.originalUser);
    }
    console.log('filter : ', JSON.stringify(filter))
    console.log('searchParams : ', JSON.stringify(searchParams), searchParams)

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

  private extractData (res: Response) {
    let body = res.json();
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
    console.error(errMsg);
    return Promise.reject(errMsg);
  }
}

export class PluginUserSearchCriteria {
  authType: string;
  authRole: string;
  userName: string;
  originalUser: string;
}
