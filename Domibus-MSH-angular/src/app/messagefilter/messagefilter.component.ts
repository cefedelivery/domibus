import {Component} from "@angular/core";
import {MessagefilterDialogComponent} from "./messagefilter-dialog/messagefilter-dialog.component";
import {MdDialog, MdDialogRef} from "@angular/material";
import {AlertService} from "../alert/alert.service";
import {Http, Headers, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import {MessageFilterResult} from "./messagefilterresult";
import {BackendFilterEntry} from "./backendfilterentry";
import {RoutingCriteriaEntry} from "./routingcriteriaentry";
import {CancelMessagefilterDialogComponent} from "./cancelmessagefilter-dialog/cancelmessagefilter-dialog.component";
import {isNullOrUndefined, isUndefined} from "util";
import {EditMessageFilterComponent} from "./editmessagefilter-form/editmessagefilter-form.component";

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
  enableEdit = false;

  enableMoveUp = false;
  enableMoveDown = false;

  loading: boolean = false;

  areFiltersPersisted: boolean;

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
    this.rollback = this.rows.slice();
  }


  getBackendFiltersInfo() {
    this.getMessageFilterEntries().subscribe((result: MessageFilterResult) => {
      console.log("messagefilter response: " + result);

      let newRows = [];
      this.backendFilterNames = [];
      if (!isNullOrUndefined(result.messageFilterEntries)) {
        for (let i = 0; i < result.messageFilterEntries.length; i++) {
          let currentFilter: BackendFilterEntry = result.messageFilterEntries[i];
          if (isNullOrUndefined(currentFilter)) {
            continue;
          }
          let backendEntry = new BackendFilterEntry(currentFilter.entityId, i, currentFilter.backendName, currentFilter.routingCriterias, currentFilter.persisted);
          newRows.push(backendEntry);
          if (this.backendFilterNames.indexOf(backendEntry.backendName) == -1) {
            this.backendFilterNames.push(backendEntry.backendName);
          }
        }
        this.areFiltersPersisted = result.areFiltersPersisted;

        this.rows = newRows;

        if (!this.areFiltersPersisted && this.backendFilterNames.length > 1) {
          this.alertService.error("Several filters in the table were not configured yet (Persisted flag is not checked). " +
            "It is strongly recommended to double check the filters configuration and afterwards save it.");
          this.enableSave = true;
        }
      }
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

  ngOnInit() {
    this.loading = true;

    this.getBackendFiltersInfo();
  }

  createValueProperty(cell, newProp, row) {
    switch (cell) {
      case 'from':
        this.rows[row].from = newProp;
        break;
      case 'to':
        this.rows[row].to = newProp;
        break;
      case 'action':
        this.rows[row].action = newProp;
        break;
      case 'service':
        this.rows[row].service = newProp;
        break;
    }
  }

  /*updateValueProperty(cell, cellValue, row) {
    switch (cell) {
      case 'from':
        this.rows[row.$$index].from.expression = cellValue;
        this.hasError('from', cellValue);
        break;
      case 'to':
        this.rows[row.$$index].to.expression = cellValue;
        this.hasError('to', cellValue);
        break;
      case 'action':
        this.rows[row.$$index].action.expression = cellValue;
        this.hasError('action', cellValue);
        break;
      case 'service':
        this.rows[row.$$index].service.expression = cellValue;
        this.hasError('service', cellValue);
        break;
    }
  }*/

  /*updateValue(event, cell, row) {
    this.editing[row.$$index + '-' + cell] = false;

    let edited = false;

    if (cell == 'plugin' && event.target.value.trim() != '') {
      this.rows[row.$$index].backendName = event.target.value;
      edited = true;
    }

    if (!edited) {
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
              if (!this.hasError(cell, event.target.value)) {
                routCriteria.expression = event.target.value;
              } else {
                break;
              }
            }
            this.updateValueProperty(cell, event.target.value, row);
            edited = true;
            break;
          }
        }
      }
    }

    if (!edited && event.target.value.trim() != '') {
      if (!this.hasError(cell, event.target.value)) {
        let newRC = new RoutingCriteriaEntry(null, cell, event.target.value);
        this.rows[row.$$index].routingCriterias.push(newRC);
        this.createValueProperty(cell, newRC, row);
        edited = true;
      }
    }

    if (edited) {
      this.enableSave = true;
      this.enableCancel = true;
      this.alertService.clearAlert();
    }
  }*/

  buttonNew() {
    let formRef: MdDialogRef<EditMessageFilterComponent> = this.dialog.open(EditMessageFilterComponent, {data: {backendFilterNames: this.backendFilterNames}});
    formRef.afterClosed().subscribe(result => {
      if(result == true) {
        let routingCriterias : Array<RoutingCriteriaEntry> = [];
        if(!isNullOrUndefined(formRef.componentInstance.from)) {
          routingCriterias.push(new RoutingCriteriaEntry(0,'from',formRef.componentInstance.from));
        }
        if(!isNullOrUndefined(formRef.componentInstance.to)) {
          routingCriterias.push(new RoutingCriteriaEntry(0,'to',formRef.componentInstance.to));
        }
        if(!isNullOrUndefined(formRef.componentInstance.action)) {
          routingCriterias.push(new RoutingCriteriaEntry(0,'action',formRef.componentInstance.action));
        }
        if(!isNullOrUndefined(formRef.componentInstance.service)) {
          routingCriterias.push(new RoutingCriteriaEntry(0,'service',formRef.componentInstance.service));
        }
        let backendEntry = new BackendFilterEntry(0, this.rowNumber + 1, formRef.componentInstance.plugin, routingCriterias, false);
        this.rows.push(backendEntry);

        this.enableSave = true;
        this.enableCancel = true;
      }
    });

  }

  buttonEdit() {
    let formRef: MdDialogRef<EditMessageFilterComponent> = this.dialog.open(EditMessageFilterComponent, {data: {backendFilterNames: this.backendFilterNames, edit: this.selected[0]}});
    formRef.afterClosed().subscribe(result => {
      if(result == true) {
        this.updateSelectedFrom(formRef.componentInstance.from);
        this.updateSelectedTo(formRef.componentInstance.to);
        this.updateSelectedAction(formRef.componentInstance.action);
        this.updateSelectedService(formRef.componentInstance.service);

        this.enableSave = true;
        this.enableCancel = true;
      }
    });
  }

  private deleteRoutingCriteria(rc: string) {
    let numRoutingCriterias = this.rows[this.rowNumber].routingCriterias.length;
    for (let i = 0; i < numRoutingCriterias; i++) {
      let routCriteria = this.rows[this.rowNumber].routingCriterias[i];
      if (routCriteria.name == rc) {
        this.rows[this.rowNumber].routingCriterias.splice(i, 1);
        return;
      }
    }
  }

  private createRoutingCriteria(rc: string, value: string) {
    if(value.length == 0) {
      return;
    }
    let newRC = new RoutingCriteriaEntry(null, rc, value);
    this.rows[this.rowNumber].routingCriterias.push(newRC);
    this.createValueProperty(rc, newRC, this.rowNumber);
  }

  private updateSelectedTo(value: string) {
    if(!isNullOrUndefined(this.rows[this.rowNumber].to)) {
      if(value.length == 0) {
        // delete
        this.deleteRoutingCriteria('to');
        this.rows[this.rowNumber].to.expression = '';
      } else {
        // update
        this.rows[this.rowNumber].to.expression = value;
      }
    } else {
      // create
      this.createRoutingCriteria('to', value);
    }
  }

  private updateSelectedFrom(value: string) {
    if(!isNullOrUndefined(this.rows[this.rowNumber].from)) {
      if(value.length == 0) {
        // delete
        this.deleteRoutingCriteria('from');
        this.rows[this.rowNumber].from.expression = '';
      } else {
        // update
        this.rows[this.rowNumber].from.expression = value;
      }
    } else {
      // create
      this.createRoutingCriteria('from', value);
    }
  }

  private updateSelectedAction(value: string) {
    if(!isNullOrUndefined(this.rows[this.rowNumber].action)) {
      if(value.length == 0) {
        // delete
        this.deleteRoutingCriteria('action');
        this.rows[this.rowNumber].action.expression = '';
      } else {
        // update
        this.rows[this.rowNumber].action.expression = value;
      }
    } else {
      // create
      this.createRoutingCriteria('action', value);
    }
  }

  private updateSelectedService(value: string) {
    if(!isNullOrUndefined(this.rows[this.rowNumber].service)) {
      if(value.length == 0) {
        // delete
        this.deleteRoutingCriteria('service');
        this.rows[this.rowNumber].service.expression = '';
      } else {
        // update
        this.rows[this.rowNumber].service.expression = value;
      }
    } else {
      // create
      this.createRoutingCriteria('service', value);
    }
  }

  private disableSelectionAndButtons() {
    this.selected = [];
    this.enableMoveDown = false;
    this.enableMoveUp = false;
    this.enableCancel = false;
    this.enableSave = false;
    this.enableEdit = false;
    this.enableDelete = false;
  }

  cancelDialog() {
    let dialogRef = this.dialog.open(CancelMessagefilterDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch (result) {
        case 'Yes' :
          this.disableSelectionAndButtons();
          this.getBackendFiltersInfo();

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
      switch (result) {
        case 'Save' :
          this.disableSelectionAndButtons();
          this.rollback = this.rows.slice();
          this.http.put('rest/messagefilters', JSON.stringify(this.rows), {headers: headers}).subscribe(res => {
            this.alertService.success("The operation 'update message filters' completed successfully.", false);
            this.getBackendFiltersInfo();
          }, err => {
            this.alertService.error("The operation 'update message filters' not completed successfully.", false);
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
    this.enableEdit = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    this.rows.splice(this.rowNumber, 1);

    this.selected = [];
  }

  buttonMoveUp() {
    if (this.rowNumber < 1) {
      return;
    }
    let array = this.rows.slice();
    let move = array[this.rowNumber];
    array[this.rowNumber] = array[this.rowNumber - 1];
    array[this.rowNumber - 1] = move;

    this.rows = array.slice();
    this.rowNumber--;

    if (this.rowNumber == 0) {
      this.enableMoveUp = false;
    }
    this.enableMoveDown = true;
    this.enableSave = true;
    this.enableCancel = true;
  }

  buttonMoveDown() {
    if (this.rowNumber > this.rows.length - 1) {
      return;
    }

    let array = this.rows.slice();
    let move = array[this.rowNumber];
    array[this.rowNumber] = array[this.rowNumber + 1];
    array[this.rowNumber + 1] = move;

    this.rows = array.slice();
    this.rowNumber++;

    if (this.rowNumber == this.rows.length - 1) {
      this.enableMoveDown = false;
    }
    this.enableMoveUp = true;
    this.enableSave = true;
    this.enableCancel = true;
  }

  onSelect({selected}) {
    console.log('Select Event', selected, this.selected);

    if (isNullOrUndefined(selected) || selected.length == 0) {
      // unselect
      this.enableMoveDown = false;
      this.enableMoveUp = false;
      this.enableDelete = false;
      this.enableEdit = false;

      return;
    }

    // select
    this.rowNumber = this.selected[0].$$index;

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableMoveDown = selected.length > 0 && this.rowNumber < this.rows.length - 1;
    this.enableMoveUp = selected.length > 0 && this.rowNumber > 0;
    this.enableDelete = selected.length == 1;
    this.enableEdit = selected.length == 1;
  }

  /*onActivate(event) {
    console.log('Activate Event', event);
  }*/

  /*isValidFromToService(str: string) {
    return str == '' || (/^[a-zA-Z0-9_:-]+:[a-zA-Z0-9_:-]+$/.test(str));
  }*/

  /*isValidAction(str: string) {
    return str == '' || (/^[a-zA-Z0-9_:-]+$/.test(str));
  }*/

  singleSelectCheck(row: any) {
    return this.selected.indexOf(row) === -1;
  }

  /*hasError(type: string, str: string) {
    switch (type) {
      case 'from':
      case 'to':
        if (!this.isValidFromToService(str)) {
          this.alertService.error(type.toUpperCase() + " rule is [PARTYID]:[TYPE] and '" + str + "' is not according to that.");
          return true;
        }
        return false;
      case 'action':
        if (!this.isValidAction(str)) {
          this.alertService.error(type.toUpperCase() + " rule is [ACTION] and '" + str + "' is not according to that.");
          return true;
        }
        return false;
      case 'service':
        if (!this.isValidFromToService(str)) {
          this.alertService.error(type.toUpperCase() + " rule is [SERVICE]:[TYPE] and '" + str + "' is not according to that.");
          return true;
        }
        return false;
      default:
        return false;
    }
  }*/
}
