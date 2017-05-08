import {Component} from "@angular/core";
import {Http, URLSearchParams, Response} from "@angular/http";
import {MessageLogResult} from "./messagelogresult";
import {Observable} from "rxjs";
import {AlertService} from "../alert/alert.service";
import {MessagelogDialogComponent} from "app/messagelog/messagelog-dialog/messagelog-dialog.component";
import {MdDialog} from "@angular/material";

@Component({
  moduleId: module.id,
  templateUrl: 'messagelog.component.html',
  providers: [],
  styleUrls: ['./messagelog.component.css']
})

export class MessageLogComponent {

  static readonly RESEND_URL: string = 'rest/';
  static readonly MESSAGE_LOG_URL: string = 'rest/messagelog';

  selected = [];

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  timestampFromMaxDate: Date = new Date();
  timestampToMinDate: Date = null;
  timestampToMaxDate: Date = new Date();

  notifiedFromMaxDate: Date = new Date();
  notifiedToMinDate: Date = null;
  notifiedToMaxDate: Date = new Date();

  filter: any = {};
  loading: boolean = false;
  rows = [];
  count: number = 0;
  offset: number = 0;
  //default value
  orderBy: string = "messageId";
  //default value
  asc: boolean = false;

  ROW_LIMITS = [
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];
  rowLimits: Array<any> = this.ROW_LIMITS;
  pageSize: number = this.ROW_LIMITS[0].value;

  mshRoles: Array<String>;
  msgTypes: Array<String>;
  msgStatus: Array<String>;
  notifStatus: Array<String>;

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  ngOnInit() {
    this.page(this.offset, this.pageSize, this.orderBy, this.asc);
  }

  getMessageLogEntries(offset: number, pageSize: number, orderBy: string, asc: boolean): Observable<MessageLogResult> {
    let params: URLSearchParams = new URLSearchParams();
    params.set('page', offset.toString());
    params.set('pageSize', pageSize.toString());
    params.set('orderBy', orderBy);

    //filters
    if (this.filter.messageId) {
      params.set('messageId', this.filter.messageId);
    }

    if (this.filter.mshRole) {
      params.set('mshRole', this.filter.mshRole);
    }

    if (this.filter.conversationId) {
      params.set('conversationId', this.filter.conversationId);
    }

    if (this.filter.messageType) {
      params.set('messageType', this.filter.messageType);
    }

    if (this.filter.messageStatus) {
      params.set('messageStatus', this.filter.messageStatus);
    }

    if (this.filter.notificationStatus) {
      params.set('notificationStatus', this.filter.notificationStatus);
    }

    if (this.filter.fromPartyId) {
      params.set('fromPartyId', this.filter.fromPartyId);
    }

    if (this.filter.toPartyId) {
      params.set('toPartyId', this.filter.toPartyId);
    }

    if (this.filter.originalSender) {
      params.set('originalSender', this.filter.originalSender);
    }

    if (this.filter.finalRecipient) {
      params.set('finalRecipient', this.filter.finalRecipient);
    }

    if (this.filter.refToMessageId) {
      params.set('refToMessageId', this.filter.refToMessageId);
    }

    if (this.filter.receivedFrom) {
      params.set('receivedFrom', this.filter.receivedFrom.getTime());
    }

    if (this.filter.receivedTo) {
      params.set('receivedTo', this.filter.receivedTo.getTime());
    }

    if (asc != null) {
      params.set('asc', asc.toString());
    }

    return this.http.get(MessageLogComponent.MESSAGE_LOG_URL, {
      search: params
    }).map((response: Response) =>
      response.json()
    );
  }

  page(offset, pageSize, orderBy, asc) {
    this.loading = true;

    this.getMessageLogEntries(offset, pageSize, orderBy, asc).subscribe((result: MessageLogResult) => {
      console.log("messageLog response:" + result);
      this.offset = offset;
      this.pageSize = pageSize;
      this.orderBy = orderBy;
      this.asc = asc;
      this.count = result.count;

      const start = offset * pageSize;
      const end = start + pageSize;
      const newRows = [...result.messageLogEntries];

      let index = 0;
      for (let i = start; i < end; i++) {
        newRows[i] = result.messageLogEntries[index++];
      }

      this.rows = newRows;

      if (result.filter.receivedFrom != null) {
        result.filter.receivedFrom = new Date(result.filter.receivedFrom);
      }
      if (result.filter.receivedTo != null) {
        result.filter.receivedTo = new Date(result.filter.receivedTo);
      }

      this.filter = result.filter;
      this.mshRoles = result.mshRoles;
      this.msgTypes = result.msgTypes;
      this.msgStatus = result.msgStatus;
      this.notifStatus = result.notifStatus;
      this.loading = false;
    }, (error: any) => {
      console.log("error getting the error log:" + error);
      this.loading = false;
      this.alertService.error("Error occured:" + error);
    });
  }

  onPage(event) {
    console.log('Page Event', event);
    this.page(event.offset, event.pageSize, this.orderBy, this.asc);
  }

  onSort(event) {
    console.log('Sort Event', event);
    let ascending = true;
    if (event.newValue === 'desc') {
      ascending = false;
    }
    this.page(this.offset, this.pageSize, event.column.prop, ascending);
  }

  onSelect({selected}) {
    console.log('Select Event', selected, this.selected);
  }

  onActivate(event) {
    console.log('Activate Event', event);
  }

  changeRowLimits(event) {
    let newPageLimit = event.value;
    console.log('New page limit:', newPageLimit);
    this.page(0, newPageLimit, this.orderBy, this.asc);
  }

  search() {
    console.log("Searching using filter:" + this.filter);
    this.page(0, this.pageSize, this.orderBy, this.asc);
  }

  resendDialog() {
    let dialogRef = this.dialog.open(MessagelogDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch (result) {
        case 'Resend' :
          this.resend(this.selected[0].messageId);
          break;
        case 'Cancel' :
        //do nothing
      }
    });
  }

  resend(messageId: string) {
    console.log('Resending message with id ', messageId);
  }

  isResendButtonEnabled() {
    if (this.selected && this.selected[0] && !this.selected[0].deleted && this.selected[0].messageStatus === "SEND_FAILURE")
      return true;

    return false;
  }

  isDownloadButtonEnabled(): boolean {
    if (this.selected && this.selected[0] && !this.selected[0].deleted)
      return true;

    return false;
  }

  downloadMessage() {

  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  onNotifiedFromChange(event) {
    this.notifiedToMinDate = event.value;
  }

  onNotifiedToChange(event) {
    this.notifiedFromMaxDate = event.value;
  }
}
