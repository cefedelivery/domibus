import {Component} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "../alert/alert.service";
/**
 * Created by tiago on 10/04/2017.
 */

@Component({
  moduleId: module.id,
  templateUrl: 'messagefilter.component.html',
  providers: [],
  styleUrls: ['./messagefilter.component.css']
})

export class MessageFilterComponent {

  plugins: Array<String>;

  enableCancel = false;
  enableSave = false;
  enableDelete = false;

  constructor(private http: Http, private alertService: AlertService) {

  }

  buttonNew() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;
  }

  buttonCancel() {
    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;
  }

  buttonSave() {
    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;
  }

  buttonDelete() {
    this.enableDelete = false;
  }

}
