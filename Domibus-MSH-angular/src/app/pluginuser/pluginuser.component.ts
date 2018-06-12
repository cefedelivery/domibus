import {Component, OnInit} from '@angular/core';
import {ColumnPickerBase} from 'app/common/column-picker/column-picker-base';
import {RowLimiterBase} from 'app/common/row-limiter/row-limiter-base';
import {AlertService} from '../alert/alert.service';
import {AlertComponent} from '../alert/alert.component';
import {PluginUserService, PluginUserSearchCriteria} from './pluginuser.service';
import {PluginUserRO} from './pluginuser';
import {DirtyOperations} from 'app/common/dirty-operations';
import {MdDialog, MdDialogRef} from '@angular/material';
import {EditPluginUserFormComponent} from './editpluginuser-form/editpluginuser-form.component';

@Component({
  templateUrl: './pluginuser.component.html',
  styleUrls: ['./pluginuser.component.css'],
  providers: [PluginUserService]
})
export class PluginUserComponent implements OnInit, DirtyOperations {

  columnPickerBasic: ColumnPickerBase = new ColumnPickerBase();
  columnPickerCert: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  offset: number = 0;
  users: PluginUserRO[] = [];
  selected = [];
  loading: boolean = false;
  dirty: boolean = false;

  authenticationTypes: string[] = ['BASIC', 'CERTIFICATE'];
  filter: PluginUserSearchCriteria = {authType: 'BASIC', authRole: '', userName: '', originalUser: ''};

  constructor (private alertService: AlertService, private pluginUserService: PluginUserService, public dialog: MdDialog) {
    this.initColumns();
  }

  ngOnInit () {
    this.users = [];
    this.dirty = false;

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
  }

  columnPicker (): ColumnPickerBase {
    return this.filter.authType == 'CERTIFICATE' ? this.columnPickerCert : this.columnPickerBasic;
  }

  async search () {
    try {
      this.loading = true;
      const result = await this.pluginUserService.getUsers(this.filter).toPromise();
      this.users = result.entries;
      this.loading = false;
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

  canSaveAsCSV (): boolean {
    return !this.loading && this.users.length > 0 && this.users.length < AlertComponent.MAX_COUNT_CSV;
  }

  onActivate (event) {
    if ('dblclick' === event.type) {
      this.edit(event.row);
    }
  }

  canEdit () {
    return this.selected.length === 1;
  }

  add () {
    const newItem = this.pluginUserService.createNew();
    newItem.authenticationType = this.filter.authType;
    this.users.push(newItem);

    this.selected.length = 0;
    this.selected.push(newItem);
  }

  edit (row) {
    row = row || this.selected[0];
    const rowCopy = JSON.parse(JSON.stringify(row));

    const formRef: MdDialogRef<EditPluginUserFormComponent> = this.dialog.open(EditPluginUserFormComponent, {
      data: {
        edit: true,
        //user: this.users[rowNumber],
        user: rowCopy,
        //userroles: this.userRoles,
      }
    });
    formRef.afterClosed().subscribe(ok => {
      if (ok) {
        if (JSON.stringify(row) === JSON.stringify(rowCopy))
          return; // nothing changed
        Object.assign(row, rowCopy);
      }
    });

  }
}
