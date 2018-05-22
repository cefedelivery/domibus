import {Component, TemplateRef, ViewChild, Renderer2, AfterViewInit, ElementRef} from "@angular/core";
import {Observable} from "rxjs";
import {Http, Response, URLSearchParams} from "@angular/http";
import {ErrorLogResult} from "./errorlogresult";
import {AlertService} from "../alert/alert.service";
import {ErrorlogDetailsComponent} from "app/errorlog/errorlog-details/errorlog-details.component";
import {MdDialog, MdDialogRef} from "@angular/material";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {DownloadService} from "../download/download.service";
import {AlertComponent} from "../alert/alert.component";
import { Md2Datepicker } from "md2";

@Component({
  moduleId: module.id,
  templateUrl: 'errorlog.component.html',
  providers: [],
  styleUrls: ['./errorlog.component.css']
})

export class ErrorLogComponent implements AfterViewInit {

  columnPicker: ColumnPickerBase = new ColumnPickerBase()
  rowLimiter: RowLimiterBase = new RowLimiterBase()

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  @ViewChild('rowWithDateFormatTpl') rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('bibi') bibi: Md2Datepicker;

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
  orderBy: string = "timestamp";
  //default value
  asc: boolean = false;

  mshRoles: Array<String>;
  errorCodes: Array<String>;

  advancedSearch: boolean;

  static readonly ERROR_LOG_URL : string = 'rest/errorlogs';
  static readonly ERROR_LOG_CSV_URL : string = ErrorLogComponent.ERROR_LOG_URL + '/csv';

  constructor(private elementRef: ElementRef, private http: Http, private alertService: AlertService, public dialog: MdDialog, private renderer: Renderer2) {
  }

  ngOnInit() {
    this.columnPicker.allColumns = [
      {
        name: "Signal Message Id",
        prop: "errorSignalMessageId"
      },
      {
        name: "AP Role",
        prop: "mshRole",
        width: 50
      },
      {
        name: 'Message Id',
        prop: "messageInErrorId",
      },
      {
        name: 'Error Code',
        width: 50
      },
      {
        name: 'Error Detail',
        width: 350
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Timestamp',
        width: 180
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Notified'
      }

    ];


    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["Message Id", "Error Code", "Timestamp"].indexOf(col.name) != -1
    });

    this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc);

    // this.bibi.registerOnValidatorChange(() => {
    //   console.log('bau')
    // })
    // this.bibi.registerOnTouched(() => {
    //   console.log('bau touch');
    //   return {};
    // });
    //
    // this.bibi._onTouched = () => {  console.log('wwwhat?')}
  //  this.bibi._validatorOnChange
  }

  onTest() {
    console.log('onTest')
  }

  getErrorLogEntries(offset: number, pageSize: number, orderBy: string, asc: boolean): Observable<ErrorLogResult> {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());
    searchParams.set('orderBy', orderBy);

    //filter
    if (this.filter.errorSignalMessageId) {
      searchParams.set('errorSignalMessageId', this.filter.errorSignalMessageId);
    }
    if (this.filter.mshRole) {
      searchParams.set('mshRole', this.filter.mshRole);
    }
    if (this.filter.messageInErrorId) {
      searchParams.set('messageInErrorId', this.filter.messageInErrorId);
    }
    if (this.filter.errorCode) {
      searchParams.set('errorCode', this.filter.errorCode);
    }
    if (this.filter.errorDetail) {
      searchParams.set('errorDetail', this.filter.errorDetail);
    }
    if (this.filter.timestampFrom != null) {
      searchParams.set('timestampFrom', this.filter.timestampFrom.getTime());
    }
    if (this.filter.timestampTo != null) {
      searchParams.set('timestampTo', this.filter.timestampTo.getTime());
    }
    if (this.filter.notifiedFrom != null) {
      searchParams.set('notifiedFrom', this.filter.notifiedFrom.getTime());
    }
    if (this.filter.notifiedTo != null) {
      searchParams.set('notifiedTo', this.filter.notifiedTo.getTime());
    }

    if (asc != null) {
      searchParams.set('asc', asc.toString());
    }

    return this.http.get(ErrorLogComponent.ERROR_LOG_URL, {
      search: searchParams
    }).map((response: Response) =>
      response.json()
    );
  }

  page(offset, pageSize, orderBy, asc) {
    this.loading = true;

    this.getErrorLogEntries(offset, pageSize, orderBy, asc).subscribe((result: ErrorLogResult) => {
      console.log("errorLog response:" + result);
      this.offset = offset;
      this.rowLimiter.pageSize = pageSize;
      this.orderBy = orderBy;
      this.asc = asc;
      this.count = result.count;

      const start = offset * pageSize;
      const end = start + pageSize;
      const newRows = [...result.errorLogEntries];

      let index = 0;
      for (let i = start; i < end; i++) {
        newRows[i] = result.errorLogEntries[index++];
      }

      this.rows = newRows;

      if (result.filter.timestampFrom != null) {
        result.filter.timestampFrom = new Date(result.filter.timestampFrom);
      }
      if (result.filter.timestampTo != null) {
        result.filter.timestampTo = new Date(result.filter.timestampTo);
      }
      if (result.filter.notifiedFrom != null) {
        result.filter.notifiedFrom = new Date(result.filter.notifiedFrom);
      }
      if (result.filter.notifiedTo != null) {
        result.filter.notifiedTo = new Date(result.filter.notifiedTo);
      }

      this.filter = result.filter;
      this.mshRoles = result.mshRoles;
      this.errorCodes = result.errorCodes;

      this.loading = false;

      if(this.count > AlertComponent.MAX_COUNT_CSV) {
        this.alertService.error("Maximum number of rows reached for downloading CSV");
      }
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

  changePageSize(newPageLimit: number) {
    console.log('New page limit:', newPageLimit);
    this.page(0, newPageLimit, this.orderBy, this.asc);
  }

  search() {
    console.log("Searching using filter:" + this.filter);
    this.page(0, this.rowLimiter.pageSize, this.orderBy, this.asc);
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

  toggleAdvancedSearch(): boolean {
    this.advancedSearch = !this.advancedSearch;
    return false;//to prevent default navigation
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

  details(selectedRow: any) {
    let dialogRef: MdDialogRef<ErrorlogDetailsComponent> = this.dialog.open(ErrorlogDetailsComponent);
    dialogRef.componentInstance.message = selectedRow;
    // dialogRef.componentInstance.currentSearchSelectedSource = this.currentSearchSelectedSource;
    dialogRef.afterClosed().subscribe(result => {
      //Todo:
    });
  }

  private getFilterPath() {
    let result = '?';

    //filter
    if (this.filter.errorSignalMessageId) {
      result += 'errorSignalMessageId=' + this.filter.errorSignalMessageId + '&';
    }
    if (this.filter.mshRole) {
      result += 'mshRole=' + this.filter.mshRole + '&';
    }
    if (this.filter.messageInErrorId) {
      result += 'messageInErrorId=' + this.filter.messageInErrorId + '&';
    }
    if (this.filter.errorCode) {
      result += 'errorCode=' + this.filter.errorCode + '&';
    }
    if (this.filter.errorDetail) {
      result += 'errorDetail=' + this.filter.errorDetail + '&';
    }
    if (this.filter.timestampFrom != null) {
      result += 'timestampFrom=' + this.filter.timestampFrom.getTime() + '&';
    }
    if (this.filter.timestampTo != null) {
      result += 'timestampTo=' + this.filter.timestampTo.getTime() + '&';
    }
    if (this.filter.notifiedFrom != null) {
      result += 'notifiedFrom=' + this.filter.notifiedFrom.getTime() + '&';
    }
    if (this.filter.notifiedTo != null) {
      result += 'notifiedTo=' + this.filter.notifiedTo.getTime() + '&';
    }

    return result;
  }

  isSaveAsCSVButtonEnabled() {
    return (this.count < AlertComponent.MAX_COUNT_CSV);
  }

  saveAsCSV() {
    DownloadService.downloadNative(ErrorLogComponent.ERROR_LOG_CSV_URL + this.getFilterPath());
  }

  ngAfterViewInit() {
   // var inputs = this.elementRef.nativeElement.getElementsByTagName('input');
   // console.log( 'inputs[0] ', inputs )
   // inputs[2].onblur =  () => { console.log('on blur ');
   //    if (!this.filter.timestampFrom)
   //      inputs[2].value = '';
   // }

  }

  onClick(event) {
    console.log(event);
  }

}
