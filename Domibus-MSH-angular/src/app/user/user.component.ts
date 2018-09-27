import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {UserResponseRO, UserState} from './user';
import {UserSearchCriteria, UserService} from './user.service';
import {MdDialog, MdDialogRef} from '@angular/material';
import {UserValidatorService} from 'app/user/uservalidator.service';
import {AlertService} from '../alert/alert.service';
import {EditUserComponent} from 'app/user/edituser-form/edituser-form.component';
import {isNullOrUndefined} from 'util';
import {Headers, Http} from '@angular/http';
import {DirtyOperations} from '../common/dirty-operations';
import {CancelDialogComponent} from '../common/cancel-dialog/cancel-dialog.component';
import {SaveDialogComponent} from '../common/save-dialog/save-dialog.component';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {SecurityService} from '../security/security.service';
import {DownloadService} from '../download/download.service';
import {AlertComponent} from '../alert/alert.component';
import {DomainService} from '../security/domain.service';
import {Domain} from '../security/domain';

@Component({
  moduleId: module.id,
  templateUrl: 'user.component.html',
  providers: [UserService, UserValidatorService],
  styleUrls: ['./user.component.css']
})


export class UserComponent implements OnInit, DirtyOperations {
  static readonly USER_URL: string = 'rest/user';
  static readonly USER_USERS_URL: string = UserComponent.USER_URL + '/users';
  static readonly USER_CSV_URL: string = UserComponent.USER_URL + '/csv';

  @ViewChild('passwordTpl') passwordTpl: TemplateRef<any>;
  @ViewChild('editableTpl') editableTpl: TemplateRef<any>;
  @ViewChild('checkBoxTpl') checkBoxTpl: TemplateRef<any>;
  @ViewChild('deletedTpl') deletedTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  users: Array<UserResponseRO>;
  userRoles: Array<String>;
  domains: Domain[];
  currentDomain: Domain;

  selected: any[];

  enableCancel: boolean;
  enableSave: boolean;
  enableDelete: boolean;
  enableEdit: boolean;

  currentUser: UserResponseRO;

  editedUser: UserResponseRO;

  dirty: boolean;
  areRowsDeleted: boolean;

  filter: UserSearchCriteria;
  deletedStatuses: any[];
  offset: number;

  isBusy = false;

  constructor (private http: Http,
               private userService: UserService,
               public dialog: MdDialog,
               private userValidatorService: UserValidatorService,
               private alertService: AlertService,
               private securityService: SecurityService,
               private domainService: DomainService) {
  }

  async ngOnInit () {
    this.isBusy = true;
    this.offset = 0;
    this.filter = new UserSearchCriteria();
    this.deletedStatuses = [null, true, false];

    this.columnPicker = new ColumnPickerBase();
    this.rowLimiter = new RowLimiterBase();

    this.users = [];
    this.userRoles = [];

    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;
    this.enableEdit = false;
    this.currentUser = null;
    this.editedUser = null;

    this.selected = [];

    this.columnPicker.allColumns = [
      {
        cellTemplate: this.editableTpl,
        name: 'Username',
        prop: 'userName',
        canAutoResize: true
      },
      {
        cellTemplate: this.editableTpl,
        name: 'Role',
        prop: 'roles',
        canAutoResize: true
      },
      {
        cellTemplate: this.editableTpl,
        name: 'Email',
        prop: 'email',
        canAutoResize: true
      },
      {
        cellTemplate: this.passwordTpl,
        name: 'Password',
        prop: 'password',
        canAutoResize: true,
        sortable: false,
        width: 25
      },
      {
        cellTemplate: this.checkBoxTpl,
        name: 'Active',
        canAutoResize: true,
        width: 25
      },
      {
        cellTemplate: this.deletedTpl,
        name: 'Deleted',
        canAutoResize: true,
        width: 25
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 60,
        canAutoResize: true,
        sortable: false
      }
    ];

    const showDomain = await this.userService.isDomainVisible();
    if (showDomain) {
      this.getUserDomains();

      this.columnPicker.allColumns.splice(2, 0,
        {
          cellTemplate: this.editableTpl,
          name: 'Domain',
          prop: 'domainName',
          canAutoResize: true
        });
    }
    this.domainService.getCurrentDomain().subscribe((domain: Domain) => this.currentDomain = domain);

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Username', 'Role', 'Domain', 'Active', 'Deleted', 'Actions'].indexOf(col.name) !== -1
    });

    this.getUsers();

    this.getUserRoles();

    this.dirty = false;
    this.areRowsDeleted = false;
  }

  getUsers (): void {
    this.isBusy = true;
    this.userService.getUsers(this.filter).subscribe(results => {
      const domains = this.domains;
      if(domains) {
        results.forEach(user => {
          const domain = domains.find(d => d.code == user.domain);
          if (domain)
            user.domainName = domain.name;
        });
      }
      this.users = results;
      this.isBusy = false;
    }, err => {
      this.isBusy = false;
    });
    this.dirty = false;
    this.areRowsDeleted = false;
  }

  getUserRoles (): void {
    this.userService.getUserRoles().subscribe(userroles => this.userRoles = userroles);
  }

  async getUserDomains (): Promise<Domain[]> {
    var res = await this.domainService.getDomains();
    this.domains = res;
    return res;
  }

  onSelect ({selected}) {
    if (isNullOrUndefined(selected) || selected.length == 0) {
      // unselect
      this.enableDelete = false;
      this.enableEdit = false;

      return;
    }

    // select
    this.currentUser = this.selected[0];
    this.editedUser = this.currentUser;

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableDelete = selected.length > 0 && !selected.every(el => el.deleted);
    this.enableEdit = selected.length == 1 && !selected[0].deleted;
  }

  private isLoggedInUserSelected (selected): boolean {
    for (let entry of selected) {
      if (this.securityService.getCurrentUser().username === entry.userName) {
        return true;
      }
    }
    return false;
  }

  buttonNew (): void {
    if (this.isBusy) return;

    this.setPage(this.getLastPage());

    this.editedUser = new UserResponseRO('', this.currentDomain, '', '', true, UserState[UserState.NEW], [], false, false);
    this.setIsDirty();
    const formRef: MdDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {
      data: {
        edit: false,
        user: this.editedUser,
        userroles: this.userRoles,
        userdomains: this.domains
      }
    });
    formRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.onSaveEditForm(formRef);

        this.users.push(this.editedUser);
        this.currentUser = this.editedUser;
      } else {
        this.selected = [];
        this.enableEdit = false;
        this.enableDelete = false;
        this.setIsDirty();
      }
    });
  }

  buttonEdit () {
    if (this.currentUser && this.currentUser.deleted) {
      this.alertService.error('You cannot edit a deleted user.', false, 3000);
      return;
    }
    this.buttonEditAction(this.currentUser);
  }

  buttonEditAction (currentUser) {
    if (this.isBusy) return;

    const formRef: MdDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {
      data: {
        edit: true,
        user: currentUser,
        userroles: this.userRoles,
        userdomains: this.domains
      }
    });
    formRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.onSaveEditForm(formRef);
      }
    });
  }

  private onSaveEditForm (formRef: MdDialogRef<EditUserComponent>) {
    const editForm = formRef.componentInstance;
    const user = this.editedUser;
    if (!user) return;

    user.userName = editForm.userName || user.userName; // only for add
    user.email = editForm.email;
    user.roles = editForm.role.toString();
    user.domain = editForm.domain;
    user.password = editForm.password;
    user.active = editForm.active;

    if (editForm.userForm.dirty) {
      if (UserState[UserState.PERSISTED] === user.status) {
        user.status = UserState[UserState.UPDATED]
      }
    }

    this.setIsDirty();
  }

  setIsDirty () {
    this.dirty = this.areRowsDeleted || this.users.filter(el => el.status !== UserState[UserState.PERSISTED]).length > 0;

    this.enableSave = this.dirty;
    this.enableCancel = this.dirty;
  }

  buttonDelete () {
    this.deleteUsers(this.selected);
  }

  buttonDeleteAction (row) {
    this.deleteUsers([row]);
  }

  private deleteUsers (users: UserResponseRO[]) {
    if (this.isLoggedInUserSelected(users)) {
      this.alertService.error('You cannot delete the logged in user: ' + this.securityService.getCurrentUser().username);
      return;
    }

    this.enableDelete = false;
    this.enableEdit = false;

    for (const itemToDelete of  users) {
      if (itemToDelete.status === UserState[UserState.NEW]) {
        this.users.splice(this.users.indexOf(itemToDelete), 1);
      } else {
        itemToDelete.status = UserState[UserState.REMOVED];
        itemToDelete.deleted = true;
      }
    }

    this.selected = [];
    this.areRowsDeleted = true;
    this.setIsDirty();
  }

  private disableSelectionAndButtons () {
    this.selected = [];
    this.enableCancel = false;
    this.enableSave = false;
    this.enableEdit = false;
    this.enableDelete = false;
  }

  cancelDialog () {
    this.dialog.open(CancelDialogComponent).afterClosed().subscribe(result => {
      if (result) {
        this.disableSelectionAndButtons();
        this.users = [];
        this.getUsers();
      }
    });
  }

  save (withDownloadCSV: boolean) {
    try {
      const isValid = this.userValidatorService.validateUsers(this.users);
      if (!isValid) return;

      this.dialog.open(SaveDialogComponent).afterClosed().subscribe(result => {
        if (result) {
          this.disableSelectionAndButtons();
          const modifiedUsers = this.users.filter(el => el.status !== UserState[UserState.PERSISTED]);
          this.isBusy = true;
          this.http.put(UserComponent.USER_USERS_URL, modifiedUsers).subscribe(res => {
            this.isBusy = false;
            this.getUsers();
            this.alertService.success('The operation \'update users\' completed successfully.', false);
            if (withDownloadCSV) {
              DownloadService.downloadNative(UserComponent.USER_CSV_URL);
            }
          }, err => {
            this.isBusy = false;
            this.getUsers();
            this.alertService.exception('The operation \'update users\' not completed successfully.', err, false);
          });
        } else {
          if (withDownloadCSV) {
            DownloadService.downloadNative(UserComponent.USER_CSV_URL);
          }
        }
      });
    } catch (err) {
      this.isBusy = false;
      this.alertService.exception('The operation \'update users\' completed with errors.', err);
    }
  }

  /**
   * Saves the content of the datatable into a CSV file
   */
  saveAsCSV () {
    if (this.isDirty()) {
      this.save(true);
    } else {
      if (this.users.length > AlertComponent.MAX_COUNT_CSV) {
        this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
        return;
      }

      DownloadService.downloadNative(UserComponent.USER_CSV_URL);
    }
  }

  isDirty (): boolean {
    return this.enableCancel;
  }

  changePageSize (newPageLimit: number) {
    this.rowLimiter.pageSize = newPageLimit;
    this.getUsers();
  }

  onChangePage (event: any): void {
    this.setPage(event.offset);
  }

  setPage (offset: number): void {
    this.offset = offset;
  }

  getLastPage (): number {
    if (!this.users || !this.rowLimiter || !this.rowLimiter.pageSize)
      return 0;
    return Math.floor(this.users.length / this.rowLimiter.pageSize);
  }
}
