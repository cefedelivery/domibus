import {Component} from "@angular/core";
import {MessagefilterDialogComponent} from "./messagefilter-dialog/messagefilter-dialog.component";
import {MdDialog} from "@angular/material";
import {AlertService} from "../alert/alert.service";
import {Http, Headers, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import {MessageFilterResult} from "./messagefilterresult";
import {BackendFilterEntry} from "./backendfilterentry";
import {RoutingCriteriaEntry} from "./routingcriteriaentry";
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

  private static NEW_VALUE = '<>';

  editing = {};
  rows = [];
  selected = [];
  rollback = [];

  rowNumber = -1;

  enableCancel = false;
  enableSave = false;
  enableDelete = false;

  enableMoveUp = false;
  enableMoveDown = false;

  loading: boolean = false;

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
    this.rollback = this.rows.slice();
  }



  ngOnInit() {
    this.loading = true;

    this.getMessageFilterEntries().subscribe((result: MessageFilterResult) => {
      console.log("messagefilter response: " + result);

      let newRows = [];
      for(let i = 0 ; i < result.length; i++) {
        if(result[i] == null) {
          continue;
        }
        let currentFilter = result[i];
        let backendEntry = new BackendFilterEntry(currentFilter.entityId, currentFilter.index, currentFilter.backendName, currentFilter.routingCriterias);
        newRows.push(backendEntry);
      }

      this.rows = newRows;
    }, (error: any) => {
      console.log("error getting the message filter: " + error);
      this.loading = false;
      this.alertService.error("Error occured: " + error);
    });
  }

  getMessageFilterEntries(): Observable<MessageFilterResult> {
    return this.http.get('rest/messagefilters').map((response: Response) =>
      response.json()
    );
  }

  updateValue(event, cell, cellValue, row) {
    this.editing[row.$$index + '-' + cell] = false;

    let edited = false;

    let numRoutingCriterias = this.rows[row.$$index].routingCriterias.length;
    for(let i = 0; i < numRoutingCriterias; i++) {
      if(this.rows[row.$$index].routingCriterias[i].name == cell) {
        this.rows[row.$$index].routingCriterias[i].expression = event.target.value;
        edited = true;
        break;
      }
    }

    if(cell == 'plugin') {
      this.rows[row.$$index].backendName = event.target.value;
      edited = true;
    }

    if(!edited) {
      let newRC = new RoutingCriteriaEntry(null,cell,event.target.value);
      this.rows[row.$$index].routingCriterias.push(newRC);
      edited = true;
    }

    if (edited) {
      this.enableSave = true;
      this.enableCancel = true;
    }
  }

  buttonNew() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;
    this.rows.push( {"backendName" : 'NEW'});
  }

  buttonCancel() {
    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;

    this.enableMoveUp = this.rowNumber  > 0;
    this.enableMoveDown = this.rowNumber != this.rows.length - 1;

    this.rows = this.rollback.slice();
  }

  saveDialog() {
    let headers = new Headers({'Content-Type': 'application/json'});
    let dialogRef = this.dialog.open(MessagefilterDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch(result) {
        case 'Save' :
          this.enableCancel = false;
          this.enableSave = false;
          this.rollback = this.rows.slice();
          this.http.put('rest/messagefilters', JSON.stringify(this.rows), { headers: headers }).subscribe(res => {
            this.alertService.success(res.json(), false);
          }, err => {
            this.alertService.error(err.json(), false);
          });
          break;
        case 'Cancel':
          // do nothing
      }
    });
  }

  buttonDelete() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    this.rows.splice(this.rowNumber, 1);
  }

  buttonMoveUp() {
    if(this.rowNumber < 1) {
      return;
    }
    var array = this.rows.slice();
    var move = array[this.rowNumber];
    array[this.rowNumber] = array[this.rowNumber-1];
    array[this.rowNumber-1] = move;

    this.rows = array.slice();
    this.rowNumber--;

    if(this.rowNumber == 0) {
      this.enableMoveUp = false;
    }
    this.enableMoveDown = true;
    this.enableSave = true;
    this.enableCancel = true;
  }

  buttonMoveDown() {
    if(this.rowNumber > this.rows.length - 1) {
      return;
    }

    var array = this.rows.slice();
    var move = array[this.rowNumber];
    array[this.rowNumber] = array[this.rowNumber+1];
    array[this.rowNumber+1] = move;

    this.rows = array.slice();
    this.rowNumber++;

    if(this.rowNumber == this.rows.length - 1) {
      this.enableMoveDown = false;
    }
    this.enableMoveUp = true;
    this.enableSave = true;
    this.enableCancel = true;
  }

  onSelect({ selected }) {
    console.log('Select Event', selected, this.selected);

    this.rowNumber = this.selected[0].$$index;

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableMoveDown = selected.length > 0 && this.rowNumber < this.rows.length - 1;
    this.enableMoveUp = selected.length > 0 && this.rowNumber > 0;
    this.enableDelete = selected.length == 1;
  }

  onActivate(event) {
    console.log('Activate Event', event);
  }
}
