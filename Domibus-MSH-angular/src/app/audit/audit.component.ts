import {Component, OnInit} from "@angular/core";
import {AuditService} from "./audit-service.service";
import {UserService} from "../user/user.service";
import {AlertService} from "../alert/alert.service";
import {AuditCriteria, AuditResponseRo} from "./audit";

@Component({
  selector: 'app-audit',
  providers: [AuditService, UserService],
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.css']
})
export class AuditComponent implements OnInit {

  auditTarget = [];
  existingAuditTargets = [];
  users = [];
  existingUsers = [];
  actions = [];
  existingActions = [];
  from: Date;
  to: Date;
  advancedSearch: boolean;
  rows = [];

  constructor(private auditService: AuditService, private userService: UserService, private alertService: AlertService) {

  }

  ngOnInit() {

    let userObservable = this.userService.getUserNames();
    userObservable.subscribe((userName: string) => this.existingUsers.push(userName));

    let actionObservable = this.auditService.getActions();
    actionObservable.subscribe((action: string) => this.existingActions.push(action));

    let existingTargets = this.auditService.getAuditTargets();
    existingTargets.subscribe((target: string) => this.existingAuditTargets.push(target));

  }

  toggleAdvancedSearch() {
    this.advancedSearch = !this.advancedSearch;
    return false;//to prevent default navigation
  }

  search() {
    let auditCriteria: AuditCriteria = new AuditCriteria();
    auditCriteria.auditTargetName = this.auditTarget;
    auditCriteria.user = this.users;
    auditCriteria.action = this.actions;
    auditCriteria.from = this.from;
    auditCriteria.to = this.to;
    auditCriteria.start = 1;
    auditCriteria.max = 50;
    this.rows.length = 0;
    let auditLogsOservable = this.auditService.getAuditLogs(auditCriteria);
    auditLogsOservable.subscribe((auditResponseRo: AuditResponseRo) => this.rows.push(auditResponseRo))
  }

}
