import {Injectable} from "@angular/core";
import {AlertService} from "../../alert/alert.service";
@Injectable()
export class JsonService {
  error: string;

  constructor(private alertService: AlertService) {

  }


}
