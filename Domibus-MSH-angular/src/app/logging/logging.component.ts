import {AfterViewInit, Component, ElementRef} from "@angular/core";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {Http, Response, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {LoggingLevelResult} from "./logginglevelresult";
import {AlertService} from "../alert/alert.service";


@Component({
  moduleId: module.id,
  templateUrl: 'logging.component.html',
  providers: [],
  styleUrls: ['./logging.component.css']
})

export class LoggingComponent implements AfterViewInit {

  columnPicker: ColumnPickerBase = new ColumnPickerBase()
  rowLimiter: RowLimiterBase = new RowLimiterBase()

  filter: any = {};
  loading: boolean = false;
  rows = [];
  count: number = 0;
  offset: number = 0;
  //default value
  orderBy: string = 'loggerName';
  //default value
  asc: boolean = false;


  static readonly LOGGING_URL: string = 'rest/logging/loglevel';

  constructor(private elementRef: ElementRef, private http: Http, private alertService: AlertService) {
  }


  ngOnInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Logger Name',
        prop: 'name'
      },
      {
        name: 'Logger Level',
        prop: 'level',
      }
    ];


    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Logger Name', 'Logger Level'].indexOf(col.name) != -1
    });

    this.page(this.offset, this.rowLimiter.pageSize);
  }

  createSearchParams(): URLSearchParams {
    const searchParams = new URLSearchParams();

    if (this.orderBy) {
      searchParams.set('orderBy', this.orderBy);
    }
    if (this.asc != null) {
      searchParams.set('asc', this.asc.toString());
    }

    if (this.filter.loggerName) {
      searchParams.set('name', this.filter.loggerName);
    }
    if (this.filter.showClasses) {
      searchParams.set('showClasses', this.filter.showClasses);
    }

    return searchParams;
  }

  getLoggingEntries(offset: number, pageSize: number): Observable<LoggingLevelResult> {
    const searchParams = this.createSearchParams();

    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());

    return this.http.get(LoggingComponent.LOGGING_URL, {
      search: searchParams
    }).map((response: Response) =>
      response.json()
    );
  }

  page (offset, pageSize) {
    this.loading = true;

    this.getLoggingEntries(offset, pageSize).subscribe((result: LoggingLevelResult) => {
      console.log('errorLog response:' + result);

      this.offset = offset;
      this.rowLimiter.pageSize = pageSize;
      this.count = result.count;

      const start = offset * pageSize;
      const end = start + pageSize;
      const newRows = [...result.loggingEntries];

      let index = 0;
      for (let i = start; i < end; i++) {
        newRows[i] = result.loggingEntries[index++];
      }

      this.rows = newRows;
      this.filter = result.filter;


      this.loading = false;
    }, (error: any) => {
      console.log('error getting the error log:' + error);
      this.loading = false;
      this.alertService.error('Error occurred:' + error);
    });

  }

  onPage (event) {
    this.page(event.offset, event.pageSize);
  }

  onSort (event) {
    this.orderBy = event.column.prop;
    this.asc = (event.newValue === 'desc') ? false : true;

    this.page(this.offset, this.rowLimiter.pageSize);
  }

  changePageSize (newPageLimit: number) {
    this.page(0, newPageLimit);
  }

  search () {
    console.log('Searching using filter:' + this.filter);
    this.page(0, this.rowLimiter.pageSize);
  }

  onSelect ({selected}) {
    // console.log('Select Event', selected, this.selected);
  }

  onActivate (event) {
    // console.log('Activate Event', event);
  }

  ngAfterViewInit() {
  }

}
