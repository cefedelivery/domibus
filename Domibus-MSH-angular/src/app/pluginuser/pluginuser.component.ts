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
import {UserState} from '../user/user';
import {CancelDialogComponent} from '../common/cancel-dialog/cancel-dialog.component';

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

  selected: PluginUserRO[] = [];
  loading = false;
  dirty = false;

  authenticationTypes: string[] = ['BASIC', 'CERTIFICATE'];
  filter: PluginUserSearchCriteria = {authType: 'BASIC', authRole: '', userName: '', originalUser: ''};
  columnPicker: ColumnPickerBase;

  userRoles: Array<String> = [];

  constructor (private alertService: AlertService,
               private pluginUserService: PluginUserService,
               public dialog: MdDialog) {
    this.initColumns();
  }

  ngOnInit () {
    this.users = [];
    this.dirty = false;

    this.getUserRoles();
    this.search();
  }

  get displayedUsers (): PluginUserRO[] {
    return this.users.filter(el => el.status !== UserState[UserState.DELETED]);
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

  async search () {
    this.selected = [];
    this.dirty = false;

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

  searchIfOK () {
    this.checkIsDirtyAndThen(this.search);
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
    const result = await this.pluginUserService.getUserRoles().toPromise();
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

  async add () {
    const newItem = this.pluginUserService.createNew();
    newItem.authenticationType = this.filter.authType;
    this.users.push(newItem);

    this.selected.length = 0;
    this.selected.push(newItem);

    this.setIsDirty();

    const ok = await this.openItemInEditForm(newItem, false);
    if (!ok) {
      this.users.pop();
      this.selected = [];
      this.setIsDirty();
    }
  }

  canEdit () {
    return this.selected.length === 1;
  }

  async edit (row: PluginUserRO) {
    row = row || this.selected[0];
    const rowCopy = Object.assign({}, row);

    const ok = await this.openItemInEditForm(rowCopy, true);
    if (ok) {
      if (JSON.stringify(row) !== JSON.stringify(rowCopy)) { // the object changed
        Object.assign(row, rowCopy);
        if (row.status === UserState[UserState.PERSISTED]) {
          row.status = UserState[UserState.UPDATED];
          this.setIsDirty();
        }
      }
    }
  }

  private async openItemInEditForm (rowCopy: PluginUserRO, edit = true) {
    const editForm = this.inBasicMode() ? EditbasicpluginuserFormComponent : EditcertificatepluginuserFormComponent;
    const ok = await this.dialog.open(editForm, {
      data: {
        edit: edit,
        user: rowCopy,
        userroles: this.userRoles,
      }
    }).afterClosed().toPromise();
    return ok;
  }

  canSave () {
    return this.isDirty();
  }

  setIsDirty () {
    this.dirty = this.users.filter(el => el.status !== UserState[UserState.PERSISTED]).length > 0;
  }

  canCancel () {
    return this.isDirty();
  }

  async cancel () {
    const ok = await this.dialog.open(CancelDialogComponent).afterClosed().toPromise();
    if (ok) {
      this.search();
    }
  }

  delete () {
    const itemToDelete = this.selected[0];
    if (itemToDelete.status === UserState[UserState.NEW]) {
      this.users.splice(this.users.indexOf(itemToDelete), 1);
    } else {
      itemToDelete.status = UserState[UserState.DELETED];
    }
    this.setIsDirty();
    this.selected.length = 0;
  }

  async checkIsDirtyAndThen (func: Function) {
    if (this.isDirty()) {
      const ok = await this.dialog.open(CancelDialogComponent).afterClosed().toPromise();
      if (ok) {
        func.call(this);
      }
    } else {
      func.call(this);
    }
  }

}
