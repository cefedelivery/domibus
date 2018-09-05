import {Component, EventEmitter, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {Http, Headers, Response} from '@angular/http';
import {AlertService} from '../alert/alert.service';
import {MessagesRequestRO} from './ro/messages-request-ro';
import {isNullOrUndefined} from 'util';
import {MdDialog, MdDialogRef} from '@angular/material';
import {MoveDialogComponent} from './move-dialog/move-dialog.component';
import {MessageDialogComponent} from './message-dialog/message-dialog.component';
import {CancelDialogComponent} from '../common/cancel-dialog/cancel-dialog.component';
import {DirtyOperations} from '../common/dirty-operations';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {Observable} from 'rxjs/Observable';
import {DownloadService} from '../download/download.service';
import {AlertComponent} from '../alert/alert.component';

@Component({
  selector: 'app-jms',
  templateUrl: './jms.component.html',
  styleUrls: ['./jms.component.css']
})
export class JmsComponent implements OnInit, DirtyOperations {

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  timestampFromMaxDate: Date;
  timestampToMinDate: Date;
  timestampToMaxDate: Date;

  defaultQueueSet: EventEmitter<boolean>;
  queuesInfoGot: EventEmitter<boolean>;

  @ViewChild('rowWithDateFormatTpl') rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowWithJSONTpl') rowWithJSONTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;

  queues: any[];

  currentSearchSelectedSource;

  selectedMessages: Array<any>;
  markedForDeletionMessages: Array<any>;
  loading: boolean;

  rows: Array<any>;
  request: MessagesRequestRO;
  private headers = new Headers({'Content-Type': 'application/json'});

  private _selectedSource: any;
  get selectedSource (): any {
    return this._selectedSource;
  }

  set selectedSource (value: any) {
    this._selectedSource = value;
    this.request.source = value.name;
    this.defaultQueueSet.emit();
  }

  constructor (private http: Http, private alertService: AlertService, public dialog: MdDialog) {
    this.request = new MessagesRequestRO();
  }

  ngOnInit () {
    this.timestampFromMaxDate = new Date();
    this.timestampToMinDate = null;
    this.timestampToMaxDate = new Date();

    this.defaultQueueSet = new EventEmitter(false);
    this.queuesInfoGot = new EventEmitter(false);

    this.queues = [];

    this.columnPicker.allColumns = [
      {
        name: 'ID',
        prop: 'id'
      },
      {
        name: 'JMS Type',
        prop: 'type',
        width: 80
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Time',
        prop: 'timestamp',
        width: 80
      },
      {
        name: 'Content',
        prop: 'content'

      },
      {
        cellTemplate: this.rowWithJSONTpl,
        name: 'Custom prop',
        prop: 'customProperties',
        width: 250
      },
      {
        cellTemplate: this.rowWithJSONTpl,
        name: 'JMS prop',
        prop: 'jmsproperties',
        width: 200
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 10,
        sortable: false
      }

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['ID', 'Time', 'Custom prop', 'JMS prop', 'Actions'].indexOf(col.name) != -1
    });

    // set toDate equals to now
    this.request.toDate = new Date();
    this.request.toDate.setHours(23, 59, 59, 999);

    this.selectedMessages = [];
    this.markedForDeletionMessages = [];
    this.loading = false;

    this.rows = [];

    this.loadDestinations();

    this.queuesInfoGot.subscribe(result => {
      this.setDefaultQueue('.*?[d|D]omibus.?DLQ');
    });

    this.defaultQueueSet.subscribe(result => {
      this.search();
    });

  }

  private getDestinations (): Observable<Response> {
    return this.http.get('rest/jms/destinations')
      .map(response => response.json().jmsDestinations)
      .catch((error: Response) => this.alertService.handleError('Could not load queues: ' + error));
  }

  private loadDestinations (): Observable<Response> {
    const result = this.getDestinations();
    result.subscribe(
      (destinations) => {
        this.queues = [];
        for (const key in destinations) {
          this.queues.push(destinations[key]);
        }
        this.queuesInfoGot.emit();
      }
    );

    return result;
  }

  private refreshDestinations (): Observable<Response> {
    const result = this.getDestinations();
    result.subscribe(
      (destinations) => {
        for (const key in destinations) {
          const queue = this.queues.find(el => el.name === key);
          if (queue) {
            Object.assign(queue, destinations[key]);
          }
        }
      }
    );
    return result;
  }

  private setDefaultQueue (queueName: string) {
    if (!this.queues || this.queues.length == 0) return;

    const matching = this.queues.find((el => el.name && el.name.match(queueName)));
    const toSelect = matching != null ? matching : this.queues.length[0];

    this.selectedSource = toSelect;
  }

  changePageSize (newPageSize: number) {
    this.rowLimiter.pageSize = newPageSize;
    this.search();
  }

  onSelect ({selected}) {
    this.selectedMessages.splice(0, this.selectedMessages.length);
    this.selectedMessages.push(...selected);
  }

  onActivate (event) {
    if ('dblclick' === event.type) {
      this.details(event.row);
    }
  }

  onTimestampFromChange (event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange (event) {
    this.timestampFromMaxDate = event.value;
  }

  canSearch () {
    return this.request.source && !this.loading;
  }

  search () {
    if (!this.request.source) {
      this.alertService.error('Source should be set');
      return;
    }
    if (this.loading) {
      return;
    }

    this.loading = true;
    this.selectedMessages = [];
    this.markedForDeletionMessages = [];
    this.currentSearchSelectedSource = this.selectedSource;
    this.http.post('rest/jms/messages', {
      source: this.request.source,
      jmsType: this.request.jmsType,
      fromDate: !isNullOrUndefined(this.request.fromDate) ? this.request.fromDate.getTime() : undefined,
      toDate: !isNullOrUndefined(this.request.toDate) ? this.request.toDate.getTime() : undefined,
      selector: this.request.selector,
    }, {headers: this.headers}).subscribe(
      (response: Response) => {
        this.rows = response.json().messages;
        this.loading = false;

        this.refreshDestinations();
      },
      error => {
        this.alertService.error('An error occured while loading the JMS messages. In case you are using the Selector / JMS Type, please follow the rules for Selector / JMS Type according to Help Page / Admin Guide (Error Status: ' + error.status + ')');
        this.loading = false;
      }
    );
  }

  cancel () {
    let dialogRef: MdDialogRef<CancelDialogComponent> = this.dialog.open(CancelDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.search();
      }
    });
  }

  save () {
    let messageIds = this.markedForDeletionMessages.map((message) => message.id);
    //because the user can change the source after pressing search and then select the messages and press delete
    //in this case I need to use currentSearchSelectedSource
    this.serverRemove(this.currentSearchSelectedSource.name, messageIds);
  }

  move () {
    const dialogRef: MdDialogRef<MoveDialogComponent> = this.dialog.open(MoveDialogComponent);

    if (/DLQ/.test(this.currentSearchSelectedSource.name)) {

      if (this.selectedMessages.length > 1) {
        dialogRef.componentInstance.queues.push(...this.queues);
      } else {
        for (let message of this.selectedMessages) {

          try {
            let originalQueue = message.customProperties.originalQueue;
            // EDELIVERY-2814
            let originalQueueName = originalQueue.substr(originalQueue.indexOf('!') + 1);
            if (!isNullOrUndefined(originalQueue)) {
              let queues = this.queues.filter((queue) => queue.name.indexOf(originalQueueName) != -1);
              console.debug(queues);
              if (!isNullOrUndefined(queues)) {
                dialogRef.componentInstance.queues = queues;
                dialogRef.componentInstance.selectedSource = queues[0];
              }
              if (queues.length == 1) {
                dialogRef.componentInstance.destinationsChoiceDisabled = true;
              }
              break;
            }
          }
          catch (e) {
            console.error(e);
          }
        }


        if (dialogRef.componentInstance.queues.length == 0) {
          console.warn('Unable to determine the original queue for the selected messages');
          dialogRef.componentInstance.queues.push(...this.queues);
        }
      }
    } else {
      dialogRef.componentInstance.queues.push(...this.queues);
    }


    dialogRef.afterClosed().subscribe(result => {
      if (!isNullOrUndefined(result) && !isNullOrUndefined(result.destination)) {
        let messageIds = this.selectedMessages.map((message) => message.id);
        this.serverMove(this.currentSearchSelectedSource.name, result.destination, messageIds);
      }
    });
  }

  moveAction (row) {
    let dialogRef: MdDialogRef<MoveDialogComponent> = this.dialog.open(MoveDialogComponent);

    if (/DLQ/.test(this.currentSearchSelectedSource.name)) {
      try {
        let originalQueue = row.customProperties.originalQueue;
        // EDELIVERY-2814
        let originalQueueName = originalQueue.substr(originalQueue.indexOf('!') + 1);
        let queues = this.queues.filter((queue) => queue.name.indexOf(originalQueueName) != -1);
        console.debug(queues);
        if (!isNullOrUndefined(queues)) {
          dialogRef.componentInstance.queues = queues;
          dialogRef.componentInstance.selectedSource = queues[0];
        }
        if (queues.length == 1) {
          dialogRef.componentInstance.destinationsChoiceDisabled = true;
        }
      }
      catch (e) {
        console.error(e);
      }

      if (dialogRef.componentInstance.queues.length == 0) {
        console.log(dialogRef.componentInstance.queues.length);
        dialogRef.componentInstance.queues.push(...this.queues);
      }
    } else {
      dialogRef.componentInstance.queues.push(...this.queues);
    }

    dialogRef.afterClosed().subscribe(result => {
      if (!isNullOrUndefined(result) && !isNullOrUndefined(result.destination)) {
        let messageIds = this.selectedMessages.map((message) => message.id);
        this.serverMove(this.currentSearchSelectedSource.name, result.destination, messageIds);
      }
    });
  }

  details (selectedRow: any) {
    let dialogRef: MdDialogRef<MessageDialogComponent> = this.dialog.open(MessageDialogComponent);
    dialogRef.componentInstance.message = selectedRow;
    dialogRef.componentInstance.currentSearchSelectedSource = this.currentSearchSelectedSource;
    dialogRef.afterClosed().subscribe(result => {
      //Todo:
    });
  }

  deleteAction (row) {
    this.markedForDeletionMessages.push(row);
    let newRows = this.rows.filter((element) => {
      return row !== element;
    });
    this.selectedMessages = [];
    this.rows = newRows;
  }

  delete () {
    this.markedForDeletionMessages.push(...this.selectedMessages);
    let newRows = this.rows.filter((element) => {
      return !this.selectedMessages.includes(element);
    });
    this.selectedMessages = [];
    this.rows = newRows;
  }

  serverMove (source: string, destination: string, messageIds: Array<any>) {
    console.log('serverMove');
    this.http.post('rest/jms/messages/action', {
      source: source,
      destination: destination,
      selectedMessages: messageIds,
      action: 'MOVE'
    }, {headers: this.headers}).subscribe(
      () => {
        this.alertService.success('The operation \'move messages\' completed successfully.');

        //refresh destinations
        this.refreshDestinations().subscribe((response: Response) => {
          this.setDefaultQueue(this.currentSearchSelectedSource.name);
        });

        //remove the selected rows
        let newRows = this.rows.filter((element) => {
          return !this.selectedMessages.includes(element);
        });
        this.selectedMessages = [];
        this.rows = newRows;
      },
      error => {
        this.alertService.error('The operation \'move messages\' could not be completed: ' + error);
      }
    )
  }

  serverRemove (source: string, messageIds: Array<any>) {
    this.http.post('rest/jms/messages/action', {
      source: source,
      selectedMessages: messageIds,
      action: 'REMOVE'
    }, {headers: this.headers}).subscribe(
      () => {
        this.alertService.success('The operation \'updates on message(s)\' completed successfully.');
        this.refreshDestinations();
        this.markedForDeletionMessages = [];
      },
      error => {
        this.alertService.error('The operation \'updates on message(s)\' could not be completed: ' + error);
      }
    )
  }

  getFilterPath () {
    let result = '?';
    if (!isNullOrUndefined(this.request.source)) {
      result += 'source=' + this.request.source + '&';
    }
    if (!isNullOrUndefined(this.request.jmsType)) {
      result += 'jmsType=' + this.request.jmsType + '&';
    }
    if (!isNullOrUndefined(this.request.fromDate)) {
      result += 'fromDate=' + this.request.fromDate.getTime() + '&';
    }
    if (!isNullOrUndefined(this.request.toDate)) {
      result += 'toDate=' + this.request.toDate.getTime() + '&';
    }
    if (!isNullOrUndefined(this.request.selector)) {
      result += 'selector=' + this.request.selector + '&';
    }
    return result;
  }

  saveAsCSV () {
    if (!this.request.source) {
      this.alertService.error('Source should be set');
      return;
    }
    if (this.rows.length > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
      return;
    }

    DownloadService.downloadNative('rest/jms/csv' + this.getFilterPath());
  }

  isDirty (): boolean {
    return !isNullOrUndefined(this.markedForDeletionMessages) && this.markedForDeletionMessages.length > 0;
  }

}
