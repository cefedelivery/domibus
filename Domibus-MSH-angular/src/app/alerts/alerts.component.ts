import {Component, TemplateRef, ViewChild} from "@angular/core";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {isNullOrUndefined} from "util";
import {DownloadService} from "../download/download.service";
import {MessageLogComponent} from "../messagelog/messagelog.component";
import {AlertComponent} from "../alert/alert.component";

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

  aTypes = ['MSG_COMMUNICATION_FAILURE','MSG_TEST'];
  aLevels = ['HIGH', 'MEDIUM', 'LOW'];

  filter: any = {};

  dynamicFilters = [];

  items=[];

  timestampCreationFromMaxDate: Date = new Date();
  timestampCreationToMaxDate: Date = new Date();
  timestampCreationToMinDate: Date = new Date();
  timestampReportingFromMaxDate: Date = new Date();
  timestampReportingToMinDate: Date = new Date();
  timestampReportingToMaxDate: Date = new Date();

  ngOnInit() {

    this.rowLimiter.pageSize = 1;
    this.columnPicker.allColumns = [
      { name: 'Processed' },
      { name: 'ID' },
      { name: 'Alert Type' },
      { name: 'Level' },
      { name: 'Alert Text' },
      { name: 'Creation Time' },
      { name: 'Reporting Time' },
      { name: 'Parameters' },
      { name: 'Actions', cellTemplate: this.rowActions, sortable: false }
    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["Processed", "ID", "Alert Type", "Level", "Alert Type", "Creation Time", "Reporting Time", "Parameters", "Actions"].indexOf(col.name) != -1
    });
  }

  search() {
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

    /*if ("dblclick" === event.type) {
      this.details(event.row);
    }*/
  }

  onPage(event) {
    console.log('Page Event', event);
    //this.page(event.offset, event.pageSize, this.orderBy, this.asc);
  }

  onSort(event) {
    console.log('Sort Event', event);
    let ascending = true;
    if (event.newValue === 'desc') {
      ascending = false;
    }
    //this.page(this.offset, this.rowLimiter.pageSize, event.column.prop, ascending);
  }

  changePageSize(newPageLimit: number) {
    console.log('New page limit:', newPageLimit);
    this.rowLimiter.pageSize = newPageLimit;
    //this.page(0, newPageLimit);
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

  }

}
