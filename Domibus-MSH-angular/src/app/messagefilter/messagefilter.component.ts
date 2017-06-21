import {Component} from "@angular/core";
import {MessagefilterDialogComponent} from "./messagefilter-dialog/messagefilter-dialog.component";
import {MdDialog} from "@angular/material";
import {AlertService} from "../alert/alert.service";
import {Http, Headers, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import {MessageFilterResult} from "./messagefilterresult";
import {BackendFilterEntry} from "./backendfilterentry";
import {RoutingCriteriaEntry} from "./routingcriteriaentry";
import {DeleteMessagefilterDialogComponent} from "./deletemessagefilter-dialog/deletemessagefilter-dialog.component";
import {CancelMessagefilterDialogComponent} from "./cancelmessagefilter-dialog/cancelmessagefilter-dialog.component";

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

  backendFilterNames = [];

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
      this.backendFilterNames = [];
      for(let i = 0 ; i < result.length; i++) {
        if(result[i] == null) {
          continue;
        }
        let currentFilter = result[i];
        let backendEntry = new BackendFilterEntry(currentFilter.entityId, i, currentFilter.backendName, currentFilter.routingCriterias);
        newRows.push(backendEntry);
        if(this.backendFilterNames.indexOf(backendEntry.backendName) == -1) {
          this.backendFilterNames.push(backendEntry.backendName);
        }
      }

      this.rows = newRows;
    }, (error: any) => {
      console.log("error getting the message filter: " + error);
      this.loading = false;
      this.alertService.error("Error occurred: " + error);
    });
  }

  getMessageFilterEntries(): Observable<MessageFilterResult> {
    return this.http.get('rest/messagefilters').map((response: Response) =>
      response.json()
    );
  }

  createValueProperty(cell, newProp, row) {
    switch(cell) {
      case 'from':
        this.rows[row.$$index].from = newProp;
        break;
      case 'to':
        this.rows[row.$$index].to = newProp;
        break;
      case 'action':
        this.rows[row.$$index].action = newProp;
        break;
      case 'sevice':
        this.rows[row.$$index].sevice = newProp;
        break;
    }
  }

  updateValueProperty(cell, cellValue, row) {
    switch(cell) {
      case 'from':
        this.rows[row.$$index].from.expression = cellValue;
        break;
      case 'to':
        this.rows[row.$$index].to.expression = cellValue;
        break;
      case 'action':
        this.rows[row.$$index].action.expression = cellValue;
        break;
      case 'sevice':
        this.rows[row.$$index].sevice.expression = cellValue;
        break;
    }
  }

  updateValue(event, cell, row) {
    this.editing[row.$$index + '-' + cell] = false;

    let edited = false;

    if(cell == 'plugin' && event.target.value.trim() != '') {
      this.rows[row.$$index].backendName = event.target.value;
      edited = true;
    }

    if(!edited) {
      if (this.rows[row.$$index].routingCriterias == null) {
        this.rows[row.$$index].routingCriterias = [];
      }
      let numRoutingCriterias = this.rows[row.$$index].routingCriterias.length;
      for (let i = 0; i < numRoutingCriterias; i++) {
        let routCriteria = this.rows[row.$$index].routingCriterias[i];
        if (routCriteria.name == cell) {
          if (routCriteria.expression == event.target.value) {
            return;
          } else {
            if (event.target.value.trim() == '') {
              this.rows[row.$$index].routingCriterias.splice(i, 1);
            } else {
              routCriteria.expression = event.target.value;
            }
            this.updateValueProperty(cell, event.target.value, row);
            edited = true;
            break;
          }
        }
      }
    }

    if(!edited && event.target.value.trim() != '') {
      let newRC = new RoutingCriteriaEntry(null,cell,event.target.value);
      this.rows[row.$$index].routingCriterias.push(newRC);
      this.createValueProperty(cell, newRC, row);
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
    this.rows.push( {"backendName" : ''});
  }

  cancelDialog() {
    let dialogRef = this.dialog.open(CancelMessagefilterDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch(result) {
        case 'Yes' :
          this.enableCancel = false;
          this.enableSave = false;
          this.enableDelete = false;

          this.enableMoveUp = this.rowNumber  > 0;
          this.enableMoveDown = this.rowNumber != this.rows.length - 1;

          this.ngOnInit();

          break;
        case 'No':
        // do nothing
      }
    });
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
            this.alertService.success("The operation 'update message filters' completed successfully.", false);
          }, err => {
            this.alertService.error("The operation 'update message filters' not completed successfully.", false);
          });

          break;
        case 'Cancel':
          // do nothing
      }
    });
  }

  deleteDialog() {
    let dialogRef = this.dialog.open(DeleteMessagefilterDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch(result) {
        case 'Yes' :
          this.enableCancel = true;
          this.enableSave = true;
          this.enableDelete = false;

          this.enableMoveUp = false;
          this.enableMoveDown = false;

          this.rows.splice(this.rowNumber, 1);

          break;
        case 'No':
        // do nothing
      }
    });
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
