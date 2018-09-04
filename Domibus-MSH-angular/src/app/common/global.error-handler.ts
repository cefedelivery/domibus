import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {AlertService} from '../alert/alert.service';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  constructor (private injector: Injector) {
  }


  handleError (error) {

    console.error(error);

    const alertService = this.injector.get(AlertService);
    alertService.error(error);
  }

}
