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

  editing = {};
  rows = [];

  enableCancel = false;
  enableSave = false;
  enableDelete = false;

  enableMoveUp = false;
  enableMoveDown = false;

  constructor() {
    this.rows.push({
      "plugin": "a",
      "from": "a",
      "to": "a",
      "action": "a",
      "service": "a"
    });
    // this.fetch((data) => {
    //   this.rows = data;
    // });
  }

  // fetch(cb) {
  //   const req = new XMLHttpRequest();
  //   req.open('GET', `https://github.com/swimlane/ngx-datatable/tree/master/assets/data/company.json`);
  //
  //   req.onload = () => {
  //     cb(JSON.parse(req.response));
  //   };
  //
  //   req.send();
  // }

  updateValue(event, cell, cellValue, row) {
    this.editing[row.$$index + '-' + cell] = false;
    this.rows[row.$$index][cell] = event.target.value;
  }

  buttonNew() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;
    this.rows.push({
      "plugin": "<>",
      "from": "<>",
      "to": "<>",
      "action": "<>",
      "service": "<>"});
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

  buttonMoveUp() {

  }

  buttonMoveDown() {

  }


  /*plugins: Array<String>;

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
  }*/

}
