import {Component, TemplateRef, ViewChild} from '@angular/core';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {isNullOrUndefined} from 'util';
import {DownloadService} from '../download/download.service';
import {AlertComponent} from '../alert/alert.component';
import {Observable} from 'rxjs/Observable';
import {AlertsResult} from './alertsresult';
import {Http, URLSearchParams, Response, Headers} from '@angular/http';
import {AlertService} from '../alert/alert.service';
import {CancelDialogComponent} from '../common/cancel-dialog/cancel-dialog.component';
import {MdDialog} from '@angular/material';
import {SaveDialogComponent} from '../common/save-dialog/save-dialog.component';

@Component({
  moduleId: module.id,
  templateUrl: 'alerts.component.html',
  providers: []
})

export class AlertsComponent {

  @ViewChild('rowProcessed') rowProcessed: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;

  static readonly ALERTS_URL: string = 'rest/alerts';
  static readonly ALERTS_CSV_URL: string = AlertsComponent.ALERTS_URL + '/csv';
  static readonly ALERTS_TYPES_URL: string = AlertsComponent.ALERTS_URL + '/types';
  static readonly ALERTS_STATUS_URL: string = AlertsComponent.ALERTS_URL + '/status';
  static readonly ALERTS_LEVELS_URL: string = AlertsComponent.ALERTS_URL + '/levels';
  static readonly ALERTS_PARAMS_URL: string = AlertsComponent.ALERTS_URL + '/params';

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  advancedSearch: boolean;
  loading: boolean = false;

  // data table
  rows = [];
  count: number = 0;
  offset: number = 0;
  //default value
  orderBy: string = 'creationTime';
  //default value
  asc: boolean = false;

  buttonsDisabled: boolean = true;

  aTypes = [];
  aStatuses = [];
  aLevels = [];

  aProcessedValues = ['PROCESSED', 'UNPROCESSED'];

  filter: any = {};

  dynamicFilters = [];

  dynamicDatesFilter: any = {};

  nonDateParameters = [];
  alertTypeWithDate: boolean = false;

  timestampCreationFromMaxDate: Date = new Date();
  timestampCreationToMinDate: Date = null;
  timestampCreationToMaxDate: Date = new Date();
  timestampReportingFromMaxDate: Date = new Date();
  timestampReportingToMinDate: Date = null;
  timestampReportingToMaxDate: Date = new Date();

  dateFromName: string = '';
  dateToName: string = '';

  constructor (private http: Http, private alertService: AlertService, public dialog: MdDialog) {
    this.getAlertTypes();
    this.getAlertLevels();
    this.getAlertStatuses();
  }

  getAlertTypes (): void {
    this.http.get(AlertsComponent.ALERTS_TYPES_URL)
      .map(this.extractData)
      .catch(this.handleError)
      .subscribe(aTypes => this.aTypes = aTypes);
  }

  getAlertStatuses (): void {
    this.http.get(AlertsComponent.ALERTS_STATUS_URL)
      .map(this.extractData)
      .catch(this.handleError)
      .subscribe(aStatuses => this.aStatuses = aStatuses);
  }

  getAlertLevels (): void {
    this.http.get(AlertsComponent.ALERTS_LEVELS_URL)
      .map(this.extractData)
      .catch(this.handleError)
      .subscribe(aLevels => this.aLevels = aLevels);
  }

  private extractData (res: Response) {
    let body = res.json();
    return body || {};
  }

  private handleError (error: Response | any) {
    this.alertService.error(error, false);
    let errMsg: string;
    if (error instanceof Response) {
      const body = error.json() || '';
      const err = body.error || JSON.stringify(body);
      errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    console.error(errMsg);
    return Promise.reject(errMsg);
  }

  ngOnInit () {
    this.columnPicker.allColumns = [
      {name: 'Alert Id', width: 20, prop: 'entityId'},
      {name: 'Processed', cellTemplate: this.rowProcessed, width: 20},
      {name: 'Alert Type'},
      {name: 'Alert Level', width: 50},
      {name: 'Alert Status', width: 50},
      {name: 'Creation Time', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Reporting Time', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Parameters', sortable: false},
      {name: 'Sent Attempts', width: 50, prop: 'attempts',},
      {name: 'Max Attempts', width: 50},
      {name: 'Next Attempt', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Reporting Time Failure', cellTemplate: this.rowWithDateFormatTpl, width: 155}
    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Processed', 'Alert Type', 'Alert Level', 'Alert Status', 'Creation Time', 'Reporting Time', 'Parameters'].indexOf(col.name) != -1
    });

    this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc);
  }

  getAlertsEntries (offset: number, pageSize: number, orderBy: string, asc: boolean): Observable<AlertsResult> {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());
    searchParams.set('orderBy', orderBy);

    // filters
    if (this.filter.processed) {
      searchParams.set('processed', this.filter.processed === 'PROCESSED' ? 'true' : 'false');
    }

    if (this.filter.alertType) {
      searchParams.set('alertType', this.filter.alertType);
    }

    if (this.filter.alertStatus) {
      searchParams.set('alertStatus', this.filter.alertStatus);
    }

    if (this.filter.alertId) {
      searchParams.set('alertId', this.filter.alertId);
    }

    if (this.filter.alertLevel) {
      searchParams.set('alertLevel', this.filter.alertLevel);
    }

    if (this.filter.creationFrom) {
      searchParams.set('creationFrom', this.filter.creationFrom.getTime());
    }

    if (this.filter.creationTo) {
      searchParams.set('creationTo', this.filter.creationTo.getTime());
    }

    if (this.filter.reportingFrom) {
      searchParams.set('reportingFrom', this.filter.reportingFrom.getTime());
    }

    if (this.filter.reportingTo) {
      searchParams.set('reportingTo', this.filter.reportingTo.getTime());
    }

    if (this.dynamicFilters.length > 0) {
      let d: string[] = [];
      for (let i = 0; i < this.dynamicFilters.length; i++) {
        d[i] = '';
      }
      for (let filter in this.dynamicFilters) {
        d[filter] = this.dynamicFilters[filter];
      }
      searchParams.set('parameters', d.toString());
    }

    if (this.alertTypeWithDate) {
      const from = this.dynamicDatesFilter.from;
      if (from) {
        searchParams.set('dynamicFrom', from.getTime());
      }

      const to = this.dynamicDatesFilter.to;
      if (to) {
        searchParams.set('dynamicTo', to.getTime());
      }
    }

    if (asc != null) {
      searchParams.set('asc', asc.toString());
    }

    return this.http.get(AlertsComponent.ALERTS_URL, {
      search: searchParams
    }).map((response: Response) =>
      response.json()
    );
  }

  page (offset, pageSize, orderBy, asc) {
    this.loading = true;

    this.getAlertsEntries(offset, pageSize, orderBy, asc).subscribe((result: AlertsResult) => {
      console.log('alerts response: ' + result);
      this.offset = offset;
      this.rowLimiter.pageSize = pageSize;
      this.orderBy = orderBy;
      this.asc = asc;
      this.count = result.count;
      const start = offset * pageSize;
      const end = start + pageSize;
      const newRows = [...result.alertsEntries];

      let index = 0;
      for (let i = start; i < end; i++) {
        newRows[i] = result.alertsEntries[index++];
      }

      this.rows = newRows;

      this.loading = false;
    }, (error: any) => {
      console.log('error getting the alerts:' + error);
      this.loading = false;
      this.alertService.error('Error occurred:' + error);
    });
  }

  search () {
    console.log('Searching using filter:' + this.filter);
    this.page(0, this.rowLimiter.pageSize, this.orderBy, this.asc);
  }

  toggleAdvancedSearch () {
    this.advancedSearch = !this.advancedSearch;
    return false;//to prevent default navigation
  }

  getAlertParameters (alertType: string): Observable<Array<string>> {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('alertType', alertType);
    return this.http.get(AlertsComponent.ALERTS_PARAMS_URL, {search: searchParams}).map(this.extractData);

  }

  onAlertTypeChanged (alertType: string) {
    this.nonDateParameters = [];
    this.alertTypeWithDate = false;
    this.dynamicFilters = [];
    this.dynamicDatesFilter = [];
    let alertParametersObservable = this.getAlertParameters(alertType).flatMap(value => value);
    const TIME_SUFFIX = '_TIME';
    const DATE_SUFFIX = '_DATE';
    let nonDateParamerters = alertParametersObservable.filter(value => {
      console.log('Value:' + value);
      return (value.search(TIME_SUFFIX) === -1 && value.search(DATE_SUFFIX) === -1)
    });
    nonDateParamerters.subscribe(item => this.nonDateParameters.push(item));
    let dateParameters = alertParametersObservable.filter(value => {
      return value.search(TIME_SUFFIX) > 0 || value.search(DATE_SUFFIX) > 1
    });
    dateParameters.subscribe(item => {
      this.dateFromName = item + ' FROM';
      this.dateToName = item + ' TO';
      this.alertTypeWithDate = true;
    });
  }

  onTimestampCreationFromChange (event) {
    this.timestampCreationToMinDate = event.value;
  }

  onTimestampCreationToChange (event) {
    this.timestampCreationFromMaxDate = event.value;
  }

  onTimestampReportingFromChange (event) {
    this.timestampReportingToMinDate = event.value;
  }

  onTimestampReportingToChange (event) {
    this.timestampReportingFromMaxDate = event.value;
  }


  // datatable methods

  onActivate (event) {
    console.log('Activate Event', event);

    // Prepared if in the future we will show details of alerts
    /*if ("dblclick" === event.type) {
      this.details(event.row);
    }*/
  }

  onPage (event) {
    console.log('Page Event', event);
    this.page(event.offset, event.pageSize, this.orderBy, this.asc);
  }

  onSort (event) {
    console.log('Sort Event', event);
    let ascending = true;
    if (event.newValue === 'desc') {
      ascending = false;
    }
    this.page(this.offset, this.rowLimiter.pageSize, event.column.prop, ascending);
  }

  changePageSize (newPageLimit: number) {
    console.log('New page limit:', newPageLimit);
    this.rowLimiter.pageSize = newPageLimit;
    this.page(0, newPageLimit, this.orderBy, this.asc);
  }

  saveAsCSV () {
    if (!this.buttonsDisabled) {
      this.save(true);
    } else {
      if (this.count > AlertComponent.MAX_COUNT_CSV) {
        this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
        return;
      }

      DownloadService.downloadNative(AlertsComponent.ALERTS_CSV_URL + this.getFilterPath());
    }
  }

  private getFilterPath () {
    let result = '?';
    //filters
    if (this.filter.processed != null) {
      result += 'processed=' + (this.filter.processed === 'PROCESSED') + '&';
    }

    if (this.filter.alertType) {
      result += 'alertType=' + this.filter.alertType + '&';
    }

    if (this.filter.alertStatus) {
      result += 'alertStatus=' + this.filter.alertStatus + '&';
    }
    if (this.filter.alertId) {
      result += 'alertId=' + this.filter.alertId + '&';
    }

    if (this.filter.alertLevel) {
      result += 'alertLevel=' + this.filter.alertLevel + '&';
    }

    if (this.filter.creationFrom) {
      result += 'creationFrom=' + this.filter.creationFrom.getTime() + '&';
    }

    if (this.filter.creationTo) {
      result += 'creationTo=' + this.filter.creationTo.getTime() + '&';
    }

    if (this.filter.reportingFrom) {
      result += 'reportingFrom=' + this.filter.reportingFrom.getTime() + '&';
    }

    if (this.filter.reportingTo) {
      result += 'reportingTo=' + this.filter.reportingTo.getTime() + '&';
    }

    return result;

  }

  public isAlertTypeDefined (): boolean {
    return !isNullOrUndefined(this.filter.alertType) && this.filter.alertType != '';
  }

  cancel () {
    this.dialog.open(CancelDialogComponent)
      .afterClosed().subscribe(result => {
      if (result) {
        this.buttonsDisabled = true;
        this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc);
      }
    });
  }

  save (withDownloadCSV: boolean) {
    let headers = new Headers({'Content-Type': 'application/json'});
    let dialogRef = this.dialog.open(SaveDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.http.put(AlertsComponent.ALERTS_URL, JSON.stringify(this.rows), {headers: headers}).subscribe(() => {
          this.alertService.success('The operation \'update alerts\' completed successfully.', false);
          this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc);
          if (withDownloadCSV) {
            DownloadService.downloadNative(AlertsComponent.ALERTS_CSV_URL);
          }
        }, err => {
          this.alertService.error('The operation \'update alerts\' not completed successfully (' + err.status + ').', false);
          this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc);
        });
      } else {
        if (withDownloadCSV) {
          DownloadService.downloadNative(AlertsComponent.ALERTS_CSV_URL);
        }
      }
    });
  }

  setProcessedValue (row) {
    this.buttonsDisabled = false;
    row.processed = !row.processed;
    this.rows[row.$$index] = row;
  }

}
