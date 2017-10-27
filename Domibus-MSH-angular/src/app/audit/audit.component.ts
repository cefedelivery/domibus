import {Component, OnInit, TemplateRef, ViewChild} from "@angular/core";
import {AuditService} from "./audit.service";
import {UserService} from "../user/user.service";
import {AlertService} from "../alert/alert.service";
import {AuditCriteria, AuditResponseRo} from "./audit";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {Observable} from "rxjs/Observable";

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * In charge of retrieving audit information from the backend.
 */

@Component({
  selector: 'app-audit',
  providers: [AuditService, UserService],
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.css']
})
export class AuditComponent implements OnInit {

//--- Search components binding ---
  existingAuditTargets = [];
  existingUsers = [];
  existingActions = [];
  auditTarget = [];
  users = [];
  actions = [];
  from: Date;
  to: Date;
  loading: boolean = false;

//--- Table binding ---
  rows = [];
  rowLimiter: RowLimiterBase = new RowLimiterBase();
  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  offset: number = 0;
  count: number = 0;
  @ViewChild('rowWithDateFormatTpl') rowWithDateFormatTpl: TemplateRef<any>;

//--- hide/show binding ---
  advancedSearch: boolean;

  constructor(private auditService: AuditService, private userService: UserService, private alertService: AlertService) {

  }

  ngOnInit() {

//--- lets init the component's data ---
    let userObservable = this.userService.getUserNames();
    userObservable.subscribe((userName: string) => this.existingUsers.push(userName));

    let actionObservable = this.auditService.listActions();
    actionObservable.subscribe((action: string) => this.existingActions.push(action));

    let existingTargets = this.auditService.listTargetTypes();
    existingTargets.subscribe((target: string) => this.existingAuditTargets.push(target));

//--- lets init the table columns ---
    this.initColumns();

//--- lets count the reccords and fill the table.---
    this.searchAndCount();
  }

  searchAndCount() {
    this.loading = true;
    this.offset = 0;
    let auditCriteria: AuditCriteria = this.buildCriteria();
    let auditLogsObservable = this.auditService.listAuditLogs(auditCriteria);
    let auditCountOservable: Observable<number> = this.auditService.countAuditLogs(auditCriteria);
    auditLogsObservable.subscribe((response: AuditResponseRo[]) => {
        this.rows = response;
        this.loading = false;
      },
      error => {
        this.alertService.error("Could not load audits " + error);
        this.loading = false;
      },
      //on complete of auditLogsObservable Observable, we load the count
      //TODO load this in parrallel and merge the stream at the end.
      () => auditCountOservable.subscribe(auditCount => this.count = auditCount,
        error => this.alertService.error("Could not count audits " + error))
    );
  }

  toggleAdvancedSearch() {
    this.advancedSearch = !this.advancedSearch;
    return false;//to prevent default navigation
  }

  searchAuditLog() {
    this.loading = true;
    let auditCriteria: AuditCriteria = this.buildCriteria();
    let auditLogsObservable = this.auditService.listAuditLogs(auditCriteria);
    auditLogsObservable.subscribe((response: AuditResponseRo[]) => {
      this.rows = response;
      this.loading = false;
    })
  }

  onPage(event) {
    console.log('Page Event', event);
    this.offset = event.offset;
    this.searchAuditLog();
  }

  buildCriteria(): AuditCriteria {
    let auditCriteria: AuditCriteria = new AuditCriteria();
    auditCriteria.auditTargetName = this.auditTarget;
    auditCriteria.user = this.users;
    auditCriteria.action = this.actions;
    auditCriteria.from = this.from;
    auditCriteria.to = this.to;
    auditCriteria.start = this.offset * this.rowLimiter.pageSize;
    auditCriteria.max = this.rowLimiter.pageSize;
    return auditCriteria;
  }

  changePageSize(newPageLimit: number) {
    this.offset = 0;
    this.rowLimiter.pageSize = newPageLimit;
    this.searchAuditLog();
  }

  initColumns() {
    this.columnPicker.allColumns = [
      {
        name: 'Table',
        prop: 'auditTargetName',
        width: 20
      },
      {
        name: 'User',
        prop: 'user',
        width: 20
      },
      {
        name: 'Action',
        prop: 'action',
        width: 20
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Changed',
        prop: 'changed',
        width: 80
      },
      {
        name: 'Id',
        prop: "id",
        width: 300
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["Table", "User", "Action", 'Changed', 'Id'].indexOf(col.name) != -1
    })
  }

}
