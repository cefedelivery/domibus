import {Component} from "@angular/core";
import {MdDialog, MdDialogRef} from "@angular/material";
import {AlertService} from "../alert/alert.service";
import {Http, Headers, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import {MessageFilterResult} from "./messagefilterresult";
import {BackendFilterEntry} from "./backendfilterentry";
import {RoutingCriteriaEntry} from "./routingcriteriaentry";
import {isNullOrUndefined, isUndefined} from "util";
import {EditMessageFilterComponent} from "./editmessagefilter-form/editmessagefilter-form.component";
import {DirtyOperations} from "../common/dirty-operations";
import {CancelDialogComponent} from "../common/cancel-dialog/cancel-dialog.component";
import {SaveDialogComponent} from "../common/save-dialog/save-dialog.component";
import {DownloadService} from "../download/download.service";

@Component({
  moduleId: module.id,
  templateUrl: 'messagefilter.component.html',
  providers: [],
  styleUrls: ['./messagefilter.component.css']
})

export class MessageFilterComponent implements DirtyOperations {

  rows = [];
  selected = [];

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

  static readonly MESSAGE_FILTER_URL: string = 'rest/messagefilters';

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
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
          this.alertService.error("One or several filters in the table were not configured yet (Persisted flag is not checked). " +
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
    return this.http.get(MessageFilterComponent.MESSAGE_FILTER_URL).map((response: Response) =>
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

  buttonNew() {
    let formRef: MdDialogRef<EditMessageFilterComponent> = this.dialog.open(EditMessageFilterComponent, {data: {backendFilterNames: this.backendFilterNames}});
    formRef.afterClosed().subscribe(result => {
      if (result == true) {
        let routingCriterias: Array<RoutingCriteriaEntry> = [];
        if (!isNullOrUndefined(formRef.componentInstance.from) && formRef.componentInstance.from != "") {
          routingCriterias.push(new RoutingCriteriaEntry(0, 'from', formRef.componentInstance.from));
        }
        if (!isNullOrUndefined(formRef.componentInstance.to) && formRef.componentInstance.to != "") {
          routingCriterias.push(new RoutingCriteriaEntry(0, 'to', formRef.componentInstance.to));
        }
        if (!isNullOrUndefined(formRef.componentInstance.action) && formRef.componentInstance.action != "") {
          routingCriterias.push(new RoutingCriteriaEntry(0, 'action', formRef.componentInstance.action));
        }
        if (!isNullOrUndefined(formRef.componentInstance.service) && formRef.componentInstance.service != "") {
          routingCriterias.push(new RoutingCriteriaEntry(0, 'service', formRef.componentInstance.service));
        }
        let backendEntry = new BackendFilterEntry(0, this.rowNumber + 1, formRef.componentInstance.plugin, routingCriterias, false);
        if (this.findRowsIndex(backendEntry) == -1) {
          this.rows.push(backendEntry);
          this.enableSave = formRef.componentInstance.messageFilterForm.dirty;
          this.enableCancel = formRef.componentInstance.messageFilterForm.dirty;
        } else {
          this.alertService.error("Impossible to insert a duplicate entry");
        }
      }
    });
  }

  private findRowsIndex(backendEntry: BackendFilterEntry): number {
    for (let i = 0; i < this.rows.length; i++) {
      let currentRow = this.rows[i];
      if (currentRow.backendName === backendEntry.backendName && this.compareRoutingCriterias(backendEntry.routingCriterias, currentRow.routingCriterias)) {
        return i;
      }
    }
    return -1;
  }

  private compareRoutingCriterias(criteriasA: RoutingCriteriaEntry[], criteriasB: RoutingCriteriaEntry[]): boolean {
    let result: boolean = true;
    for (let entry of criteriasA) {
      result = result && this.findRoutingCriteria(entry, criteriasB);
    }
    return result;
  }

  private findRoutingCriteria(toFind: RoutingCriteriaEntry, routingCriterias: RoutingCriteriaEntry[]): boolean {
    for (let entry of routingCriterias) {
      if (entry.name === toFind.name && entry.expression === toFind.expression) {
        return true;
      }
    }
    return toFind.expression === '' && routingCriterias.length == 0;
  }

  buttonEditAction(row) {
    let formRef: MdDialogRef<EditMessageFilterComponent> = this.dialog.open(EditMessageFilterComponent, {
      data: {
        backendFilterNames: this.backendFilterNames,
        edit: row
      }
    });
    formRef.afterClosed().subscribe(result => {
      if (result == true) {
        let routingCriterias: Array<RoutingCriteriaEntry> = [];
        if (!isNullOrUndefined(formRef.componentInstance.from)) {
          routingCriterias.push(new RoutingCriteriaEntry(0, 'from', formRef.componentInstance.from));
        }
        if (!isNullOrUndefined(formRef.componentInstance.to)) {
          routingCriterias.push(new RoutingCriteriaEntry(0, 'to', formRef.componentInstance.to));
        }
        if (!isNullOrUndefined(formRef.componentInstance.action)) {
          routingCriterias.push(new RoutingCriteriaEntry(0, 'action', formRef.componentInstance.action));
        }
        if (!isNullOrUndefined(formRef.componentInstance.service)) {
          routingCriterias.push(new RoutingCriteriaEntry(0, 'service', formRef.componentInstance.service));
        }
        let backendEntry = new BackendFilterEntry(0, this.rowNumber + 1, formRef.componentInstance.plugin, routingCriterias, false);
        let backendEntryPos = this.findRowsIndex(backendEntry);
        if (backendEntryPos == -1) {
          this.updateSelectedPlugin(formRef.componentInstance.plugin);
          this.updateSelectedFrom(formRef.componentInstance.from);
          this.updateSelectedTo(formRef.componentInstance.to);
          this.updateSelectedAction(formRef.componentInstance.action);
          this.updateSelectedService(formRef.componentInstance.service);

          this.enableSave = formRef.componentInstance.messageFilterForm.dirty;
          this.enableCancel = formRef.componentInstance.messageFilterForm.dirty;
        } else {
          if (this.findRowsIndex(backendEntry) != this.rowNumber) {
            this.alertService.error("Impossible to insert a duplicate entry");
          }
        }
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
    if (value.length == 0) {
      return;
    }
    let newRC = new RoutingCriteriaEntry(null, rc, value);
    this.rows[this.rowNumber].routingCriterias.push(newRC);
    this.createValueProperty(rc, newRC, this.rowNumber);
  }

  private updateSelectedTo(value: string) {
    if (!isNullOrUndefined(this.rows[this.rowNumber].to)) {
      if (value.length == 0) {
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

  private updateSelectedPlugin(value: string) {
    this.rows[this.rowNumber].backendName = value;
  }

  private updateSelectedFrom(value: string) {
    if (!isNullOrUndefined(this.rows[this.rowNumber].from)) {
      if (value.length == 0) {
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
    if (!isNullOrUndefined(this.rows[this.rowNumber].action)) {
      if (value.length == 0) {
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
    if (!isNullOrUndefined(this.rows[this.rowNumber].service)) {
      if (value.length == 0) {
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

  isSaveAsCSVButtonEnabled() : boolean {
    return (this.rows.length < 10000);
  }

  saveAsCSV() {
    if(this.isDirty()) {
      this.saveDialog(true);
    } else {
      DownloadService.downloadNative(MessageFilterComponent.MESSAGE_FILTER_URL + "/csv");
    }
  }

  cancelDialog() {
    let dialogRef = this.dialog.open(CancelDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.disableSelectionAndButtons();
        this.getBackendFiltersInfo();
      }
    });
  }

  saveDialog(withDownloadCSV: boolean) {
    let headers = new Headers({'Content-Type': 'application/json'});
    let dialogRef = this.dialog.open(SaveDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.disableSelectionAndButtons();
        this.http.put(MessageFilterComponent.MESSAGE_FILTER_URL, JSON.stringify(this.rows), {headers: headers}).subscribe(res => {
          this.alertService.success("The operation 'update message filters' completed successfully.", false);
          this.getBackendFiltersInfo();
          if(withDownloadCSV) {
            DownloadService.downloadNative(MessageFilterComponent.MESSAGE_FILTER_URL + "/csv");
          }
        }, err => {
          this.alertService.error("The operation 'update message filters' not completed successfully.", false);
        });
      } else {
        if(withDownloadCSV) {
          DownloadService.downloadNative(MessageFilterComponent.MESSAGE_FILTER_URL + "/csv");
        }
      }
    });
  }

  buttonDeleteAction(row) {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;
    this.enableEdit = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    this.rows.splice(row.$$index, 1);

    this.selected = [];
  }

  buttonDelete() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;
    this.enableEdit = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    // we need to use the old for loop approach to don't mess with the entries on the top before
    for (let i = this.selected.length - 1; i >= 0; i--) {
      this.rows.splice(this.selected[i].$$index, 1);
    }

    this.selected = [];
  }

  private moveUpInternal(rowNumber) {
    if (rowNumber < 1) {
      return;
    }
    let array = this.rows.slice();
    let move = array[rowNumber];
    array[rowNumber] = array[rowNumber - 1];
    array[rowNumber - 1] = move;

    this.rows = array.slice();
    this.rowNumber--;

    if (rowNumber == 0) {
      this.enableMoveUp = false;
    }
    this.enableMoveDown = true;
    this.enableSave = true;
    this.enableCancel = true;
  }

  buttonMoveUpAction(row) {
    this.moveUpInternal(row.$$index);
    setTimeout(() => {
      document.getElementById('pluginRow'+(row.$$index)+'_id').click();
    }, 50);
  }

  buttonMoveUp() {
    this.moveUpInternal(this.rowNumber);
  }

  private moveDownInternal(rowNumber) {
    if (rowNumber > this.rows.length - 1) {
      return;
    }

    let array = this.rows.slice();
    let move = array[rowNumber];
    array[rowNumber] = array[rowNumber + 1];
    array[rowNumber + 1] = move;

    this.rows = array.slice();
    this.rowNumber++;

    if (rowNumber == this.rows.length - 1) {
      this.enableMoveDown = false;
    }
    this.enableMoveUp = true;
    this.enableSave = true;
    this.enableCancel = true;
  }

  buttonMoveDownAction(row) {
    this.moveDownInternal(row.$$index);
    setTimeout(() => {
      document.getElementById('pluginRow'+(row.$$index)+'_id').click();
    }, 50);
  }

  buttonMoveDown() {
    this.moveDownInternal(this.rowNumber);
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
    this.enableMoveDown = selected.length == 1 && this.rowNumber < this.rows.length - 1;
    this.enableMoveUp = selected.length == 1 && this.rowNumber > 0;
    this.enableDelete = selected.length > 0;
    this.enableEdit = selected.length == 1;
  }

  isDirty(): boolean {
    return this.enableCancel;
  }
}
