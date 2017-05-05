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
  selected = [];

  enableCancel = false;
  enableSave = false;
  enableDelete = false;

  enableMoveUp = false;
  enableMoveDown = false;

  ROW_LIMITS = [
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];
  rowLimits: Array<any> = this.ROW_LIMITS;
  pageSize: number = this.ROW_LIMITS[0].value;

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

  changeRowLimits(event) {
    let newPageLimit = event.value;
    console.log('New page limit:', newPageLimit);
    this.page(newPageLimit);
  }

  page(pageSize) {
    this.pageSize = pageSize;
  }

  onSelect({ selected }) {
    console.log('Select Event', selected, this.selected);

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableMoveDown = true;
    this.enableMoveUp = true;
  }

  onActivate(event) {
    console.log('Activate Event', event);
  }

}
