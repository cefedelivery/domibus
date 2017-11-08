import {Component, EventEmitter, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {Http, Headers, Response} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {MessagesRequestRO} from "./ro/messages-request-ro";
import {isNullOrUndefined} from "util";
import {MdDialog, MdDialogRef} from "@angular/material";
import {MoveDialogComponent} from "./move-dialog/move-dialog.component";
import {MessageDialogComponent} from "./message-dialog/message-dialog.component";
import {CancelDialogComponent} from "../common/cancel-dialog/cancel-dialog.component";
import {DirtyOperations} from "../common/dirty-operations";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-jms',
  templateUrl: './jms.component.html',
  styleUrls: ['./jms.component.css']
})
export class JmsComponent implements OnInit, DirtyOperations {

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  timestampFromMaxDate: Date = new Date();
  timestampToMinDate: Date = null;
  timestampToMaxDate: Date = new Date();

  defaultQueueSet = new EventEmitter(false);
  queuesInfoGot = new EventEmitter(false);

  @ViewChild('rowWithDateFormatTpl') rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowWithJSONTpl') rowWithJSONTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;

  queues = [];

  private _selectedSource: any;
  get selectedSource(): any {
    return this._selectedSource;
  }

  set selectedSource(value: any) {
    //poor man's binding between 2 objects;
    //whenever selectedSource is set the request.source is also set
    this._selectedSource = value;
    this.request.source = value.name;
    this.defaultQueueSet.emit();
  }

  currentSearchSelectedSource;

  selectedMessages: Array<any> = [];
  markedForDeletionMessages: Array<any> = [];
  loading: boolean = false;

  rows: Array<any> = [];
  request = new MessagesRequestRO();
  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  ngOnInit() {

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
        width: 60,
        sortable: false
      }

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["ID", "Time", "Custom prop", "JMS prop", "Actions"].indexOf(col.name) != -1
    });

    // set toDate equals to now
    this.request.toDate = new Date();
    this.request.toDate.setHours(23, 59, 59, 999);

    this.getDestinations();

    this.queuesInfoGot.subscribe(result => {
      this.setDefaultQueue('.*?[d|D]omibus.?DLQ');
    });

    this.defaultQueueSet.subscribe(result => {
      this.search();
    });

  }

  private getDestinations(): Observable<Response> {
    let observableResponse: Observable<Response> = this.http.get("rest/jms/destinations");


    observableResponse.subscribe(
      (response: Response) => {
        this.queues = [];
        let destinations = response.json().jmsDestinations;
        for (let key in destinations) {
          this.queues.push(destinations[key]);
        }
        this.queuesInfoGot.emit();
      },
      (error: Response) => {
        this.alertService.error('Could not load queues: ' + error);
      }
    );

    return observableResponse
  }

  private setDefaultQueue(queueName: string) {
    this.queues.forEach(queue => {
      if (queue.name.match(queueName)) {
        this.selectedSource = queue;
      }
    });
  }

  changePageSize(newPageSize: number) {
    this.rowLimiter.pageSize = newPageSize;
    this.search();
  }

  onSelect({selected}) {
    console.log('Select Event');
    this.selectedMessages.splice(0, this.selectedMessages.length);
    this.selectedMessages.push(...selected);
  }

  onActivate(event) {
    console.log('Activate Event', event);

    if ("dblclick" === event.type) {
      this.details(event.row);
    }
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  updateQueuesInfo() {
    let observableResponse: Observable<Response> = this.http.get("rest/jms/destinations");

    observableResponse.subscribe(
      (response: Response) => {
        let destinations = response.json().jmsDestinations;
        for (let key in destinations) {
          if (key === this.selectedSource.name) {
            this.selectedSource.numberOfMessages = destinations[key].numberOfMessages;
          }
        }
      },
      (error: Response) => {
        this.alertService.error('Could not load queues: ' + error);
      }
    );

    return observableResponse
  }

  search() {
    this.loading = true;
    this.selectedMessages = [];
    this.markedForDeletionMessages = [];
    this.currentSearchSelectedSource = this.selectedSource;
    this.http.post("rest/jms/messages", {
      source: this.request.source,
      jmsType: this.request.jmsType,
      fromDate: !isNullOrUndefined(this.request.fromDate) ? this.request.fromDate.getTime() : undefined,
      toDate: !isNullOrUndefined(this.request.toDate) ? this.request.toDate.getTime() : undefined,
      selector: this.request.selector,
    }, {headers: this.headers}).subscribe(
      (response: Response) => {
        this.rows = response.json().messages;
        this.loading = false;

        this.updateQueuesInfo();

      },
      error => {
        this.alertService.error('An error occured while loading the JMS messages. In case you are using the Selector / JMS Type, please follow the rules for Selector/JMS Type according to Help Page / Admin Guide (Error Status: ' + error.status + ')');
        this.loading = false;
      }
    );
  }

  cancel() {
    let dialogRef: MdDialogRef<CancelDialogComponent> = this.dialog.open(CancelDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.search();
      }
    });
  }

  save() {
    let messageIds = this.markedForDeletionMessages.map((message) => message.id);
    //because the user can change the source after pressing search and then select the messages and press delete
    //in this case I need to use currentSearchSelectedSource
    this.serverRemove(this.currentSearchSelectedSource.name, messageIds);
  }

  move() {
    let dialogRef: MdDialogRef<MoveDialogComponent> = this.dialog.open(MoveDialogComponent);

    if (/DLQ/.test(this.currentSearchSelectedSource.name)) {

      if(this.selectedMessages.length > 1) {
        dialogRef.componentInstance.queues.push(...this.queues);
      } else {
        for (let message of this.selectedMessages) {
          try {
            let originalQueue = message.customProperties.originalQueue;
            if (!isNullOrUndefined(originalQueue)) {
              let queue = this.queues.filter((queue) => queue.name === originalQueue).pop();
              dialogRef.componentInstance.queues.push(queue);
              dialogRef.componentInstance.destinationsChoiceDisabled = true;
              dialogRef.componentInstance.selectedSource = queue;
              break;
            }
          }
          catch (e) {
            console.error(e);
          }
        }

        if (dialogRef.componentInstance.queues.length == 0) {
          console.warn("Unable to determine the original queue for the selected messages");
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

  details(selectedRow: any) {
    let dialogRef: MdDialogRef<MessageDialogComponent> = this.dialog.open(MessageDialogComponent);
    dialogRef.componentInstance.message = selectedRow;
    dialogRef.componentInstance.currentSearchSelectedSource = this.currentSearchSelectedSource;
    dialogRef.afterClosed().subscribe(result => {
      //Todo:
    });
  }

  deleteAction(row) {
    this.markedForDeletionMessages.push(row);
    let newRows = this.rows.filter((element) => {
      return row !== element;
    });
    this.selectedMessages = [];
    this.rows = newRows;
  }

  delete() {
    this.markedForDeletionMessages.push(...this.selectedMessages);
    let newRows = this.rows.filter((element) => {
      return !this.selectedMessages.includes(element);
    });
    this.selectedMessages = [];
    this.rows = newRows;
  }

  serverMove(source: string, destination: string, messageIds: Array<any>) {
    this.http.post("rest/jms/messages/action", {
      source: source,
      destination: destination,
      selectedMessages: messageIds,
      action: "MOVE"
    }, {headers: this.headers}).subscribe(
      () => {
        this.alertService.success("The operation 'move messages' completed successfully.");

        //refresh destinations
        this.getDestinations().subscribe((response: Response) => {
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
        this.alertService.error("The operation 'move messages' could not be completed: " + error);
      }
    )
  }

  serverRemove(source: string, messageIds: Array<any>) {
    this.http.post("rest/jms/messages/action", {
      source: source,
      selectedMessages: messageIds,
      action: "REMOVE"
    }, {headers: this.headers}).subscribe(
      () => {
        this.alertService.success("The operation 'updates on message(s)' completed successfully.");
        this.getDestinations();
        this.markedForDeletionMessages = [];
      },
      error => {
        this.alertService.error("The operation 'updates on message(s)' could not be completed: " + error);
      }
    )
  }

  isDirty(): boolean {
    return !isNullOrUndefined(this.markedForDeletionMessages) && this.markedForDeletionMessages.length > 0;
  }


}
