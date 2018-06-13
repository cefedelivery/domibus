import {Component, OnInit} from '@angular/core';
import {ColumnPickerBase} from 'app/common/column-picker/column-picker-base';
import {RowLimiterBase} from 'app/common/row-limiter/row-limiter-base';
import {AlertService} from '../alert/alert.service';
import {AlertComponent} from '../alert/alert.component';
import {PluginUserService, PluginUserSearchCriteria} from './pluginuser.service';
import {PluginUserRO} from './pluginuser';
import {DirtyOperations} from 'app/common/dirty-operations';
import {MdDialog, MdDialogRef} from '@angular/material';
import {EditbasicpluginuserFormComponent} from './editpluginuser-form/editbasicpluginuser-form.component';
import {EditcertificatepluginuserFormComponent} from './editpluginuser-form/editcertificatepluginuser-form.component';
import {UserService} from '../user/user.service';

@Component({
  templateUrl: './pluginuser.component.html',
  styleUrls: ['./pluginuser.component.css'],
  providers: [PluginUserService, UserService]
})
export class PluginUserComponent implements OnInit, DirtyOperations {

  columnPickerBasic: ColumnPickerBase = new ColumnPickerBase();
  columnPickerCert: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  offset = 0;
  users: PluginUserRO[] = [];
  selected = [];
  loading = false;
  dirty = false;

  authenticationTypes: string[] = ['BASIC', 'CERTIFICATE'];
  filter: PluginUserSearchCriteria = {authType: 'BASIC', authRole: '', userName: '', originalUser: ''};
  columnPicker: ColumnPickerBase;

  userRoles: Array<String> = [];

  constructor (private alertService: AlertService,
               private pluginUserService: PluginUserService,
               public dialog: MdDialog,
               private userService: UserService) {
    this.initColumns();
  }

  ngOnInit () {
    this.users = [];
    this.dirty = false;

    this.getUserRoles();
    this.search();
  }

  private initColumns () {
    this.columnPickerBasic.allColumns = [
      {name: 'Username', prop: 'username', width: 20},
      {name: 'Password', prop: 'password', width: 20},
      {name: 'Role', prop: 'authRoles', width: 10},
      {name: 'Original User', prop: 'originalUser', width: 240},
    ];
    this.columnPickerCert.allColumns = [
      {name: 'Certificate Id', prop: 'certificateId', width: 240},
      {name: 'Role', prop: 'authRoles', width: 10},
      {name: 'Original User', prop: 'originalUser', width: 240},
    ];

    this.columnPickerBasic.selectedColumns = this.columnPickerBasic.allColumns.filter(col => true);
    this.columnPickerCert.selectedColumns = this.columnPickerCert.allColumns.filter(col => true);

    this.setColumnPicker();
  }

  setColumnPicker () {
    this.columnPicker = this.filter.authType == 'CERTIFICATE' ? this.columnPickerCert : this.columnPickerBasic;
  }


  // columnPicker (): ColumnPickerBase {
  //   return this.filter.authType == 'CERTIFICATE' ? this.columnPickerCert : this.columnPickerBasic;
  // }

  async search () {
    console.log('search');
    try {
      this.loading = true;
      const result = await this.pluginUserService.getUsers(this.filter).toPromise();
      this.users = result.entries;
      this.loading = false;

      this.setColumnPicker();
    } catch (err) {
      this.alertService.error(err);
      this.loading = false;
    }
  }

  changePageSize (newPageSize: number) {
    this.rowLimiter.pageSize = newPageSize;
    this.search();
  }

  inBasicMode (): boolean {
    return this.filter.authType == 'BASIC';
  }

  inCertificateMode (): boolean {
    return this.filter.authType == 'CERTIFICATE';
  }

  isDirty (): boolean {
    return this.dirty;
  }

  async getUserRoles () {
    const result = await this.userService.getUserRoles().toPromise();
    this.userRoles = result;
  }

  canSaveAsCSV (): boolean {
    return !this.loading && this.users.length > 0 && this.users.length < AlertComponent.MAX_COUNT_CSV;
  }

  onActivate (event) {
    if ('dblclick' === event.type) {
      this.edit(event.row);
    }
  }

  add () {
    const newItem = this.pluginUserService.createNew();
    newItem.authenticationType = this.filter.authType;
    this.users.push(newItem);

    this.selected.length = 0;
    this.selected.push(newItem);
  }

  canEdit () {
    return this.selected.length === 1;
  }

  async edit (row) {
    row = row || this.selected[0];
    const rowCopy = Object.assign({}, row);

    const editForm = this.inBasicMode() ? EditbasicpluginuserFormComponent : EditcertificatepluginuserFormComponent;

    const ok = await this.dialog.open(editForm, {
      data: {
        edit: true,
        user: rowCopy,
        userroles: this.userRoles,
      }
    }).afterClosed().toPromise();

    if (ok) {
      if (JSON.stringify(row) === JSON.stringify(rowCopy))
        return; // nothing changed
      Object.assign(row, rowCopy);
    }
  }
}
