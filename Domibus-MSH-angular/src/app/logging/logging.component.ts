import {AfterViewInit, Component, ElementRef, TemplateRef, ViewChild} from "@angular/core";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {Headers, Http, Response, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {LoggingLevelResult} from "./logginglevelresult";
import {AlertService} from "../alert/alert.service";

/**
 * @author Catalin Enache
 * @since 4.1
 */
@Component({
  moduleId: module.id,
  templateUrl: 'logging.component.html',
  providers: [],
  styleUrls: ['./logging.component.css']
})

export class LoggingComponent implements AfterViewInit {

  columnPicker: ColumnPickerBase = new ColumnPickerBase()
  rowLimiter: RowLimiterBase = new RowLimiterBase()

  @ViewChild('rowWithToggleTpl') rowWithToggleTpl: TemplateRef<any>;

  levels: Array<String>;

  filter: any = {};
  loading: boolean = false;
  rows = [];
  private headers = new Headers({'Content-Type': 'application/json'});
  count: number = 0;
  offset: number = 0;
  //default value
  orderBy: string = 'loggerName';
  //default value
  asc: boolean = false;


  static readonly LOGGING_URL: string = 'rest/logging/loglevel';
  static readonly RESET_LOGGING_URL: string = 'rest/logging/reset';

  constructor(private elementRef: ElementRef, private http: Http, private alertService: AlertService) {
  }


  ngOnInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Logger Name',
        prop: 'name'
      },
      {
        cellTemplate: this.rowWithToggleTpl,
        name: 'Logger Level'
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
      searchParams.set('loggerName', this.filter.loggerName);
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
      this.levels = result.levels;

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

  onLevelChange(newLevel: string, row: any) {
    console.log('level changed from: ' + row.level + ' to: ' + newLevel + ' for: ' + row.name);
    if (newLevel !== row.level) {
      this.alertService.clearAlert();
      this.http.post(LoggingComponent.LOGGING_URL, {
        name: row.name,
        level: newLevel,
      }, {headers: this.headers}).subscribe(
        (response: Response) => {
          this.page(this.offset, this.rowLimiter.pageSize);
        },
        error => {
          this.alertService.error('An error occurred while setting logging level (Error Status: ' + error.status + ')');
          this.loading = false;
        }
      );
    }
  }

  resetLogging() {
    console.log('Reset button clicked!');
    this.http.post(LoggingComponent.RESET_LOGGING_URL, {}, {headers: this.headers}).subscribe(
      (response: Response) => {
        this.alertService.success('Logging configuration was successfully reset.', false);
        this.page(this.offset, this.rowLimiter.pageSize);
      },
      error => {
        this.alertService.error('An error occurred while resetting logging (Error Status: ' + error.status + ')');
        this.loading = false;
      }
    );
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
