import {Injectable} from '@angular/core';
import {
  Http, Response, RequestOptionsArgs, Headers, RequestOptions, ConnectionBackend,
  XHRBackend
} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/share';
import 'rxjs/add/observable/throw';
import {Observer} from 'rxjs/Observer';
import {HttpEventService} from "./http.event.service";

@Injectable()
export class ExtendedHttpClient extends Http {
  http: Http;
  httpEventService: HttpEventService;

  constructor(_backend: ConnectionBackend, _defaultOptions: RequestOptions, httpEventService: HttpEventService) {
    super(_backend, _defaultOptions);
    this.httpEventService = httpEventService;
  }

  setOptions(options?: RequestOptionsArgs): RequestOptionsArgs {
    if (!options) {
      options = {};
    }
    if (!options.headers) {
      options.headers = new Headers();
    }
    return options;
  }

  request(url: string, options?: RequestOptionsArgs): Observable<Response> {
    options = this.setOptions(options);

    return super.request(url, options).catch((error: Response) => {
      if ((error.status === 403)) {
        console.log('The authentication session expires or the user is not authorised');
        this.httpEventService.requestForbiddenEvent(error);
        // return Observable.empty();
      }
      return Observable.throw(error);
    });
  }

}
