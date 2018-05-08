import {Component, TemplateRef, ViewChild} from "@angular/core";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {isNullOrUndefined} from "util";
import {DownloadService} from "../download/download.service";
import {AlertComponent} from "../alert/alert.component";
import {Observable} from "rxjs/Observable";
import {AlertsResult} from "./alertsresult";
import {Http, URLSearchParams, Response} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {AlertsEntry} from "./alertsentry";

@Component({
  moduleId: module.id,
  templateUrl: 'alerts.component.html',
  providers: []
})

export class AlertsComponent {

  @ViewChild('rowActions') rowActions: TemplateRef<any>;

  static readonly ALERTS_URL: string = 'rest/alerts';

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  advancedSearch: boolean;
  loading: boolean = false;

  // data table
  rows = [];
  selected = [];
  count: number = 0;
  offset: number = 10;
  //default value
  orderBy: string = "creationTime";
  //default value
  asc: boolean = false;

  aTypes = ['MSG_COMMUNICATION_FAILURE','MSG_TEST'];
  aLevels = ['HIGH', 'MEDIUM', 'LOW'];
  aProcessedValues = ['PROCESSED', 'UNPROCESSED'];

  filter: any = {};

  dynamicFilters = [];

  items=[];

  timestampCreationFromMaxDate: Date = new Date();
  timestampCreationToMaxDate: Date = new Date();
  timestampCreationToMinDate: Date = new Date();
  timestampReportingFromMaxDate: Date = new Date();
  timestampReportingToMinDate: Date = new Date();
  timestampReportingToMaxDate: Date = new Date();

  constructor(private http: Http, private alertService: AlertService) {

  }

  ngOnInit() {
    this.filter.alertType = null;

    this.columnPicker.allColumns = [
      { name: 'Processed' },
      { name: 'Alert Id' },
      { name: 'Alert Type' },
      { name: 'Alert Level' },
      { name: 'Alert Text' },
      { name: 'Creation Time' },
      { name: 'Reporting Time' },
      { name: 'Parameters', sortable: false },
      { name: 'Actions', cellTemplate: this.rowActions, sortable: false }
    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["Processed", "ID", "Alert Type", "Alert Level", "Creation Time", "Reporting Time", "Parameters", "Actions"].indexOf(col.name) != -1
    });

    this.page(this.offset, this.rowLimiter.pageSize, this.orderBy, this.asc);
  }

  getAlertsEntries(offset: number, pageSize: number, orderBy: string, asc: boolean): Observable<AlertsResult> {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());
    searchParams.set('orderBy', orderBy);

    // filters
    if(this.filter.processed) {
      searchParams.set('processed', this.filter.processed);
    }

    if(this.filter.alertType) {
      searchParams.set('alertType', this.filter.alertType);
    }

    if(this.filter.alertId) {
      searchParams.set('alertId', this.filter.alertId);
    }

    if(this.filter.alertLevel) {
      searchParams.set('alertLevel', this.filter.alertLevel);
    }

    if(this.filter.creationFrom) {
      searchParams.set('creationFrom', this.filter.creationFrom.getTime());
    }

    if(this.filter.creationTo) {
      searchParams.set('creationTo', this.filter.creationTo.getTime());
    }

    if(this.filter.reportingFrom) {
      searchParams.set('reportingFrom', this.filter.reportingFrom.getTime());
    }

    if(this.filter.reportingTo) {
      searchParams.set('reportingTo', this.filter.reportingTo.getTime());
    }

    if(this.dynamicFilters.length > 0) {
      let d : string[];
      for(let i = 0; i < this.dynamicFilters.length; i++) {
        d[i] = '';
      }
      for(let filter in this.dynamicFilters) {
        d[filter] = this.dynamicFilters[filter];
      }
      searchParams.set('parameters', d.toString());
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

  page(offset, pageSize, orderBy, asc) {
    this.loading = true;

    let newEntries: AlertsEntry[] = [];
    let entry1: AlertsEntry = new AlertsEntry(true, 'alertId1', 'alertType1', this.aLevels[0], 'aText1', new Date(), new Date(), new Date(), new Date(), []);
    let entry2: AlertsEntry = new AlertsEntry(true, 'alertId2', 'alertType2', this.aLevels[1], 'aText2', new Date(), new Date(), new Date(), new Date(), []);
    let entry3: AlertsEntry = new AlertsEntry(true, 'alertId3', 'alertType3', this.aLevels[0], 'aText3', new Date(), new Date(), new Date(), new Date(), []);


    newEntries[0] = entry1;
    newEntries[1] = entry2;
    newEntries[2] = entry3;


    this.rows = newEntries;

    this.count = 3;
    this.offset = offset;
    this.rowLimiter.pageSize = pageSize;
    this.orderBy = orderBy;
    this.asc = asc;
    this.selected = [];
    this.loading = false;

    /*this.getAlertsEntries(offset, pageSize, orderBy, asc).subscribe( (result: AlertsResult) => {
      console.log("alerts response: " + result);
      this.offset = offset;
      this.rowLimiter.pageSize = pageSize;
      this.orderBy = orderBy;
      this.asc = asc;
      this.count = result.count;
      this.selected = [];

      const start = offset * pageSize;
      const end = start + pageSize;
      const newRows = [...result.alertsEntries];

      let index = 0;
      for(let i = start; i <end; i++) {
        newRows[i] = result.alertsEntries[index++];
      }
      this.rows = newRows;

      this.filter = result.filter;
      this.aLevels = result.alertsLevels;
      this.aTypes = result.alertsType;

      this.loading = false;

      if(this.count > AlertComponent.MAX_COUNT_CSV) {
        this.alertService.error("Maximum number of rows reached for downloading CSV");
      }
    }, (error: any) => {
      console.log("error getting the alerts:" + error);
      this.loading = false;
      this.alertService.error("Error occured:" + error);
    });*/
  }

  search() {
    console.log("Searching using filter:" + this.filter);
    this.page(0, this.rowLimiter.pageSize, this.orderBy, this.asc);
  }

  toggleAdvancedSearch() {
    this.advancedSearch = !this.advancedSearch;
    return false;//to prevent default navigation
  }

  onAlertTypeChanged(alertType: string) {
    if(!isNullOrUndefined(alertType) && alertType != '') {
      // just for testing begin
      if(alertType == 'MSG_COMMUNICATION_FAILURE') {
        this.items = ['MSG_COMM1', 'MSG_COMM2', 'MSG_COMM3']
      } else {
        this.items = ['MSG_TEST1', 'MSG_TEST2'];
      }
      // just for testing end
    } else {
      this.items = [];
    }
  }

  onTimestampCreationFromChange(event) {
    this.timestampCreationFromMaxDate = event.value;
  }

  onTimestampCreationToChange(event) {
    this.timestampCreationToMaxDate = event.value;
  }

  onTimestampReportingFromChange(event) {
    this.timestampReportingFromMaxDate = event.value;
  }

  onTimestampReportingToChange(event) {
    this.timestampReportingToMaxDate = event.value;
  }


  // datatable methods

  onSelect({selected}) {
    console.log('Select Event', selected, this.selected);
  }

  onActivate(event) {
    console.log('Activate Event', event);

    // Prepared if in the future we will show details of alerts
    /*if ("dblclick" === event.type) {
      this.details(event.row);
    }*/
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
    this.rowLimiter.pageSize = newPageLimit;
    this.page(0, newPageLimit, this.orderBy, this.asc);
  }

  /**
   * Method that checks if CSV Button export can be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  isSaveAsCSVButtonEnabled() : boolean {
    return this.rows.length < AlertComponent.MAX_COUNT_CSV;
  }

  saveAsCSV() {
    DownloadService.downloadNative(AlertsComponent.ALERTS_URL + "/csv" + this.getFilterPath());
  }

  private getFilterPath() {
    let result = '?';
    //filters
    if(this.filter.processed) {
      result += 'processed=' + this.filter.processed + '&';
    }

    if(this.filter.alertType) {
      result += 'alertType=' + this.filter.alertType + '&';
    }

    if(this.filter.alertId) {
      result += 'alertId=' + this.filter.alertId + '&';
    }

    if(this.filter.alertLevel) {
      result += 'alertLevel=' + this.filter.alertLevel + '&';
    }

    if(this.filter.creationFrom) {
      result += 'creationFrom=' + this.filter.creationFrom.getTime() + '&';
    }

    if(this.filter.creationTo) {
      result += 'creationTo=' + this.filter.creationTo.getTime() + '&';
    }

    if(this.filter.reportingFrom) {
      result += 'reportingFrom=' + this.filter.reportingFrom.getTime() + '&';
    }

    if(this.filter.reportingTo) {
      result += 'reportingTo=' + this.filter.reportingTo.getTime() + '&';
    }

    return result;

  }

  public isAlertTypeDefined(): boolean {
    return !isNullOrUndefined(this.filter.alertType) && this.filter.alertType != '';
  }

}
