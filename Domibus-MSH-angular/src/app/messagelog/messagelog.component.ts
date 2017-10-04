import {Component, EventEmitter, TemplateRef, ViewChild} from "@angular/core";
import {Http, URLSearchParams, Response} from "@angular/http";
import {MessageLogResult} from "./messagelogresult";
import {Observable} from "rxjs";
import {AlertService} from "../alert/alert.service";
import {MessagelogDialogComponent} from "app/messagelog/messagelog-dialog/messagelog-dialog.component";
import {MdDialog, MdDialogRef} from "@angular/material";
import {MessagelogDetailsComponent} from "app/messagelog/messagelog-details/messagelog-details.component";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";

@Component({
  moduleId: module.id,
  templateUrl: 'messagelog.component.html',
  providers: [],
  styleUrls: ['./messagelog.component.css']
})

export class MessageLogComponent {

  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('nextAttemptInfoTpl') public nextAttemptInfoTpl: TemplateRef<any>;
  @ViewChild('nextAttemptInfoWithDateFormatTpl') public nextAttemptInfoWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  static readonly RESEND_URL: string = 'rest/message/${messageId}/restore';
  static readonly DOWNLOAD_MESSAGE_URL: string = 'rest/message/${messageId}/download';
  static readonly MESSAGE_LOG_URL: string = 'rest/messagelog';

  selected = [];

  timestampFromMaxDate: Date = new Date();
  timestampToMinDate: Date = null;
  timestampToMaxDate: Date = new Date();

  filter: any = {};
  loading: boolean = false;
  rows = [];
  count: number = 0;
  offset: number = 0;
  //default value
  orderBy: string = "received";
  //default value
  asc: boolean = false;

  mshRoles: Array<String>;
  msgTypes: Array<String>;
  msgStatus: Array<String>;
  notifStatus: Array<String>;

  advancedSearch: boolean;

  messageResent = new EventEmitter(false);

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  ngOnInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Message Id',
        width: 275
      },
      {
        name: 'From Party Id'
      },
      {
        name: 'To Party Id'
      },
      {
        name: 'Message Status',
        width: 175
      },
      {
        name: 'Notification Status',
        width: 175
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Received',
        width: 155
      },
      {
        name: 'AP Role',
        prop: 'mshRole'
      },
      {
        cellTemplate: this.nextAttemptInfoTpl,
        name: 'Send Attempts'
      },
      {
        cellTemplate: this.nextAttemptInfoTpl,
        name: 'Send Attempts Max'
      },
      {
        cellTemplate: this.nextAttemptInfoWithDateFormatTpl,
        name: 'Next Attempt',
        width: 155
      },
      {
        name: 'Conversation Id'
      },
      {
        name: 'Message Type',
        width: 130
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Deleted',
        width: 155
      },
      {
        name: 'Original Sender'
      },
      {
        name: 'Final Recipient'
      },
      {
        name: 'Ref To Message Id'
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Failed',
        width: 155
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Restored',
        width: 155
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 80,
        sortable: false
      }

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["Message Id", "From Party Id", "To Party Id", "Message Status", "Received", "AP Role", "Message Type", "Actions"].indexOf(col.name) != -1
    });

    this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc)
  }

  getMessageLogEntries(offset: number, pageSize: number, orderBy: string, asc: boolean): Observable<MessageLogResult> {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());
    searchParams.set('orderBy', orderBy);

    //filters
    if (this.filter.messageId) {
      searchParams.set('messageId', this.filter.messageId);
    }

    if (this.filter.mshRole) {
      searchParams.set('mshRole', this.filter.mshRole);
    }

    if (this.filter.conversationId) {
      searchParams.set('conversationId', this.filter.conversationId);
    }

    if (this.filter.messageType) {
      searchParams.set('messageType', this.filter.messageType);
    }

    if (this.filter.messageStatus) {
      searchParams.set('messageStatus', this.filter.messageStatus);
    }

    if (this.filter.notificationStatus) {
      searchParams.set('notificationStatus', this.filter.notificationStatus);
    }

    if (this.filter.fromPartyId) {
      searchParams.set('fromPartyId', this.filter.fromPartyId);
    }

    if (this.filter.toPartyId) {
      searchParams.set('toPartyId', this.filter.toPartyId);
    }

    if (this.filter.originalSender) {
      searchParams.set('originalSender', this.filter.originalSender);
    }

    if (this.filter.finalRecipient) {
      searchParams.set('finalRecipient', this.filter.finalRecipient);
    }

    if (this.filter.refToMessageId) {
      searchParams.set('refToMessageId', this.filter.refToMessageId);
    }

    if (this.filter.receivedFrom) {
      searchParams.set('receivedFrom', this.filter.receivedFrom.getTime());
    }

    if (this.filter.receivedTo) {
      searchParams.set('receivedTo', this.filter.receivedTo.getTime());
    }

    if (asc != null) {
      searchParams.set('asc', asc.toString());
    }

    return this.http.get(MessageLogComponent.MESSAGE_LOG_URL, {
      search: searchParams
    }).map((response: Response) =>
      response.json()
    );
  }

  page(offset, pageSize, orderBy, asc) {
    this.loading = true;

    this.getMessageLogEntries(offset, pageSize, orderBy, asc).subscribe((result: MessageLogResult) => {
      console.log("messageLog response:" + result);
      this.offset = offset;
      this.rowLimiter.pageSize = pageSize;
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
    this.page(this.offset, this.rowLimiter.pageSize, event.column.prop, ascending);
  }

  onSelect({selected}) {
    // console.log('Select Event', selected, this.selected);
  }

  onActivate(event) {
    // console.log('Activate Event', event);

    if ("dblclick" === event.type) {
      this.details(event.row);
    }
  }

  changePageSize(newPageLimit: number) {
    console.log('New page limit:', newPageLimit);
    this.page(0, newPageLimit, this.orderBy, this.asc);
  }

  search() {
    console.log("Searching using filter:" + this.filter);
    this.page(0, this.rowLimiter.pageSize, this.orderBy, this.asc);
  }

  resendDialog() {
    let dialogRef = this.dialog.open(MessagelogDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch (result) {
        case 'Resend' :
          this.resend(this.selected[0].messageId);
          this.selected = [];
          this.messageResent.subscribe(result => {
            this.search();
          });
          break;
        case 'Cancel' :
        //do nothing
      }
    });
  }

  resend(messageId: string) {
    console.log('Resending message with id ', messageId);

    let url = MessageLogComponent.RESEND_URL.replace("${messageId}", messageId);

    console.log('URL is  ', url);

    this.http.put(url, {}, {}).subscribe(res => {
      this.alertService.success("The operation resend message completed successfully");
      setTimeout(() => {
        this.messageResent.emit();
      }, 500);
    }, err => {
      this.alertService.error("The message " + messageId + " could not be resent.");
    });
  }

  isResendButtonEnabledAction(row): boolean {
    return !row.deleted && row.messageStatus === "SEND_FAILURE";
  }

  isResendButtonEnabled() {
    if (this.selected && this.selected.length == 1 && !this.selected[0].deleted && this.selected[0].messageStatus === "SEND_FAILURE")
      return true;

    return false;
  }

  isDownloadButtonEnabledAction(row): boolean {
    return !row.deleted;
  }

  isDownloadButtonEnabled(): boolean {
    if (this.selected && this.selected.length == 1 && !this.selected[0].deleted)
      return true;

    return false;
  }

  private downloadMessage(messageId) {
    const url = MessageLogComponent.DOWNLOAD_MESSAGE_URL.replace("${messageId}", messageId);
    this.downloadNative(url);
  }

  downloadAction(row) {
    this.downloadMessage(row.messageId);
  }

  download() {
    this.downloadMessage(this.selected[0].messageId);
  }

  details(selectedRow: any) {
    let dialogRef: MdDialogRef<MessagelogDetailsComponent> = this.dialog.open(MessagelogDetailsComponent);
    dialogRef.componentInstance.message = selectedRow;
    // dialogRef.componentInstance.currentSearchSelectedSource = this.currentSearchSelectedSource;
    dialogRef.afterClosed().subscribe(result => {
      //Todo:
    });
  }

  toggleAdvancedSearch() {
    this.advancedSearch = !this.advancedSearch;
  }

  private downloadNative(content) {
    let element = document.createElement('a');
    element.setAttribute('href', content);
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  private showNextAttemptInfo(row: any): boolean {
    if (row && (row.messageType === "SIGNAL_MESSAGE" || row.mshRole === "RECEIVING"))
      return false;
    return true;
  }
}
