import {Component, OnInit} from '@angular/core';
import {Http, Headers, Response} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {MessagesRequestRO} from "./ro/messages-request-ro";
import {isNullOrUndefined} from "util";
import {MdDialog, MdDialogRef} from "@angular/material";
import {MoveDialogComponent} from "./move-dialog/move-dialog.component";
import {MessageDialogComponent} from "./message-dialog/message-dialog.component";

@Component({
  selector: 'app-jms',
  templateUrl: './jms.component.html',
  styleUrls: ['./jms.component.css']
})
export class JmsComponent implements OnInit {

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  timestampFromMaxDate: Date = new Date();
  timestampToMinDate: Date = null;
  timestampToMaxDate: Date = new Date();

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
  }

  currentSearchSelectedSource;

  selectedMessages: Array<any> = [];
  markedForDeletionMessages: Array<any> = [];
  loading: boolean = false;

  rows: Array<any> = [];
  pageSizes: Array<any> = [
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];
  pageSize: number = this.pageSizes[0].value;

  request = new MessagesRequestRO();
  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  ngOnInit() {
    // set toDate equals to now
    this.request.toDate = new Date()
    this.request.toDate.setHours(23,59,59,999)

    this.getDestinations(true);
  }

  private getDestinations(searchSelectedDestination: boolean) {
    this.http.get("rest/jms/destinations").subscribe(
      (response: Response) => {
        this.queues = [];
        let destinations = response.json().jmsDestinations;
        for (let key in destinations) {
          this.queues.push(destinations[key]);
          if (key.match('domibus\.DLQ')) {
            this.selectedSource = destinations[key];
          }
        }
        if (searchSelectedDestination) {
          this.search();
        }
        // console.log(this.queues);
      },
      (error: Response) => {
        this.alertService.error('Could not load queues: ' + error);
      }
    )
  }

  changePageSize(newPageSize: number) {
    this.pageSize = newPageSize;
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
        //console.log(messages);
      },
      error => {
        this.alertService.error('Could not load messages: ' + error);
        this.loading = false;
      }
    )
  }

  cancel() {
    this.search();
    this.alertService.success("The operation 'message updates cancelled' completed successfully");
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
    } else {
      dialogRef.componentInstance.queues.push(...this.queues);
    }


    dialogRef.afterClosed().subscribe(result => {
      if (!isNullOrUndefined(result) && !isNullOrUndefined(result.destination)) {
        let messageIds = this.selectedMessages.map((message) => message.id);
        this.serverMove(this.selectedSource.name, result.destination, messageIds);
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
        this.getDestinations(false);

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
        this.getDestinations(false);
        this.markedForDeletionMessages = [];
      },
      error => {
        this.alertService.error("The operation 'updates on message(s)' could not be completed: " + error);
      }
    )
  }


}
