import {Injectable} from "@angular/core";
import {Subject} from "rxjs/Subject";
import {Response} from "@angular/http";

@Injectable()
export class HttpEventService extends Subject<any> {
    constructor() {
        super();
    }
    requestForbiddenEvent(error: Response) {
        if(error) {
            super.next(error);
        }
    }
}
