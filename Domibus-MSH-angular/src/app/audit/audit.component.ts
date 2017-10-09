import {Component, OnInit} from "@angular/core";

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.css']
})
export class AuditComponent implements OnInit {

  tables = ['table1'];
  existingTables = ['table1', 'table2'];
  users = [];
  existingUsers = [];
  actions = [];
  existingActions = [];
  from: Date;
  to: Date;
  advancedSearch: boolean;
  rows = [];

  constructor() {

  }

  ngOnInit() {
  }

  toggleAdvancedSearch() {
    this.advancedSearch = !this.advancedSearch;
    return false;//to prevent default navigation
  }

}
