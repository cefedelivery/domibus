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
  rollback = [];

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

    this.rollback = this.rows.slice();
  }

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

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    this.rows = this.rollback.slice();
  }

  buttonSave() {
    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    this.rollback = this.rows.slice();
  }

  buttonDelete() {
    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;
  }

  buttonMoveUp() {

  }

  buttonMoveDown() {

  }

  onSelect({ selected }) {
    console.log('Select Event', selected, this.selected);

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableMoveDown = selected.length > 0 && this.rows.length > 1;
    this.enableMoveUp = selected.length > 0 && this.rows.length > 1;
    this.enableDelete = selected.length == 1;
  }

  onDeselect( { selected }) {
    this.enableMoveDown = selected.length > 0;
    this.enableMoveUp = selected.length > 0;
    this.enableDelete = selected.length == 1;
  }

  onActivate(event) {
    console.log('Activate Event', event);
  }
}
