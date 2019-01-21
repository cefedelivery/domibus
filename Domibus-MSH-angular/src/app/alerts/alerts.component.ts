import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {DownloadService} from '../common/download.service';
import {AlertComponent} from '../common/alert/alert.component';
import {Observable} from 'rxjs/Observable';
import {AlertsResult} from './alertsresult';
import {Http, URLSearchParams, Response, Headers} from '@angular/http';
import {AlertService} from '../common/alert/alert.service';
import {CancelDialogComponent} from '../common/cancel-dialog/cancel-dialog.component';
import {MdDialog} from '@angular/material';
import {SaveDialogComponent} from '../common/save-dialog/save-dialog.component';
import {SecurityService} from '../security/security.service';
import {FilterableListComponent} from '../common/filterable-list.component';

@Component({
  moduleId: module.id,
  templateUrl: 'alerts.component.html',
  providers: []
})

export class AlertsComponent extends FilterableListComponent implements OnInit {
  static readonly ALERTS_URL: string = 'rest/alerts';
  static readonly ALERTS_CSV_URL: string = AlertsComponent.ALERTS_URL + '/csv';
  static readonly ALERTS_TYPES_URL: string = AlertsComponent.ALERTS_URL + '/types';
  static readonly ALERTS_STATUS_URL: string = AlertsComponent.ALERTS_URL + '/status';
  static readonly ALERTS_LEVELS_URL: string = AlertsComponent.ALERTS_URL + '/levels';
  static readonly ALERTS_PARAMS_URL: string = AlertsComponent.ALERTS_URL + '/params';

  @ViewChild('rowProcessed') rowProcessed: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  advancedSearch: boolean;
  loading: boolean = false;

  // data table
  rows = [];
  count = 0;
  offset = 0;
  orderBy = 'creationTime';
  asc = false;

  isDirty = false;

  aTypes = [];
  aStatuses = [];
  aLevels = [];

  aProcessedValues = ['PROCESSED', 'UNPROCESSED'];

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
  displayDomainCheckBox: boolean = false;

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog, private securityService: SecurityService) {
    super();

    this.getAlertTypes();
    this.getAlertLevels();
    this.getAlertStatuses();
    if (this.securityService.isCurrentUserSuperAdmin()) {
      this.displayDomainCheckBox = true;
    }
  }

  ngOnInit() {
    this.filter = {processed: 'UNPROCESSED', domainAlerts: false};

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

    this.search();
  }

  getAlertTypes(): void {
    this.http.get(AlertsComponent.ALERTS_TYPES_URL)
      .map(this.extractData)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aTypes => this.aTypes = aTypes);
  }

  getAlertStatuses(): void {
    this.http.get(AlertsComponent.ALERTS_STATUS_URL)
      .map(this.extractData)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aStatuses => this.aStatuses = aStatuses);
  }

  getAlertLevels(): void {
    this.http.get(AlertsComponent.ALERTS_LEVELS_URL)
      .map(this.extractData)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aLevels => this.aLevels = aLevels);
  }

  private extractData(res: Response) {
    let body = res.json();
    return body || {};
  }

  getAlertsEntries(offset: number, pageSize: number, orderBy: string, asc: boolean): Observable<AlertsResult> {
    const searchParams = this.createSearchParams();

    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());

    return this.http.get(AlertsComponent.ALERTS_URL, {
      search: searchParams
    }).map((response: Response) =>
      response.json()
    );
  }

  private createSearchParams() {
    const searchParams = this.createStaticSearchParams();

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
    return searchParams;
  }

  private createStaticSearchParams() {
    const searchParams: URLSearchParams = new URLSearchParams();

    searchParams.set('orderBy', this.orderBy);
    if (this.asc != null) {
      searchParams.set('asc', this.asc.toString());
    }

    // filters
    if (this.activeFilter.processed) {
      searchParams.set('processed', this.activeFilter.processed === 'PROCESSED' ? 'true' : 'false');
    }

    if (this.activeFilter.alertType) {
      searchParams.set('alertType', this.activeFilter.alertType);
    }

    if (this.activeFilter.alertStatus) {
      searchParams.set('alertStatus', this.activeFilter.alertStatus);
    }

    if (this.activeFilter.alertId) {
      searchParams.set('alertId', this.activeFilter.alertId);
    }

    if (this.activeFilter.alertLevel) {
      searchParams.set('alertLevel', this.activeFilter.alertLevel);
    }

    if (this.activeFilter.creationFrom) {
      searchParams.set('creationFrom', this.activeFilter.creationFrom.getTime());
    }

    if (this.activeFilter.creationTo) {
      searchParams.set('creationTo', this.activeFilter.creationTo.getTime());
    }

    if (this.activeFilter.reportingFrom) {
      searchParams.set('reportingFrom', this.activeFilter.reportingFrom.getTime());
    }

    if (this.activeFilter.reportingTo) {
      searchParams.set('reportingTo', this.activeFilter.reportingTo.getTime());
    }

    searchParams.set('domainAlerts', this.activeFilter.domainAlerts);
    return searchParams;
  }

  page(offset, pageSize, orderBy, asc) {
    this.loading = true;
    this.resetFilters();
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

  search() {
    console.log('Searching using filter:' + this.filter);
    this.setActiveFilter();
    this.page(0, this.rowLimiter.pageSize, this.orderBy, this.asc);
  }

  toggleAdvancedSearch() {
    this.advancedSearch = !this.advancedSearch;
    return false; // to prevent default navigation
  }

  getAlertParameters(alertType: string): Observable<Array<string>> {
    const searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('alertType', alertType);
    return this.http.get(AlertsComponent.ALERTS_PARAMS_URL, {search: searchParams}).map(this.extractData);
  }

  onAlertTypeChanged(alertType: string) {
    this.nonDateParameters = [];
    this.alertTypeWithDate = false;
    this.dynamicFilters = [];
    this.dynamicDatesFilter = [];
    const alertParametersObservable = this.getAlertParameters(alertType).flatMap(value => value);
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

  onTimestampCreationFromChange(event) {
    this.timestampCreationToMinDate = event.value;
  }

  onTimestampCreationToChange(event) {
    this.timestampCreationFromMaxDate = event.value;
  }

  onTimestampReportingFromChange(event) {
    this.timestampReportingToMinDate = event.value;
  }

  onTimestampReportingToChange(event) {
    this.timestampReportingFromMaxDate = event.value;
  }


  // datatable methods

  onPage(event) {
    console.log('Page Event', event);
    this.page(event.offset, event.pageSize, this.orderBy, this.asc);
  }

  onSort(event) {
    let ascending = true;
    if (event.newValue === 'desc') {
      ascending = false;
    }
    this.page(0, this.rowLimiter.pageSize, event.column.prop, ascending);
  }

  changePageSize(newPageLimit: number) {
    this.rowLimiter.pageSize = newPageLimit;
    this.page(0, newPageLimit, this.orderBy, this.asc);
  }

  saveAsCSV() {
    if (this.isDirty) {
      this.save(true);
    } else {
      if (this.count > AlertComponent.MAX_COUNT_CSV) {
        this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
        return;
      }

      super.resetFilters();
      // todo: add dynamic params for csv filtering, if requested
      DownloadService.downloadNative(AlertsComponent.ALERTS_CSV_URL + '?' + this.createStaticSearchParams().toString());
    }
  }

  cancel() {
    this.dialog.open(CancelDialogComponent)
      .afterClosed().subscribe(result => {
      if (result) {
        this.isDirty = false;
        this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc);
      }
    });
  }

  save(withDownloadCSV: boolean) {
    const dialogRef = this.dialog.open(SaveDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.http.put(AlertsComponent.ALERTS_URL, JSON.stringify(this.rows), {headers: new Headers({'Content-Type': 'application/json'})}).subscribe(() => {
          this.alertService.success('The operation \'update alerts\' completed successfully.', false);
          this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc);
          this.isDirty = false;
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

  setProcessedValue(row) {
    this.isDirty = true;
    row.processed = !row.processed;
    this.rows[row.$$index] = row;
  }

}
