import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {UserResponseRO, UserState} from './user';
import {UserService} from './user.service';
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
  @ViewChild('rowActions') rowActions: TemplateRef<any>;

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  users: Array<UserResponseRO> = [];
  userRoles: Array<String> = [];
  domains: Domain[];
  currentDomain: Domain;

  selected = [];

  enableCancel: boolean = false;
  enableSave: boolean = false;
  enableDelete: boolean = false;
  enableEdit: boolean = false;

  rowNumber = -1;

  editedUser: UserResponseRO;

  dirty: boolean;
  areRowsDeleted: boolean;

  constructor (private http: Http,
               private userService: UserService,
               public dialog: MdDialog,
               private userValidatorService: UserValidatorService,
               private alertService: AlertService,
               private securityService: SecurityService,
               private domainService: DomainService) {
  }

  ngOnInit (): void {
    this.columnPicker = new ColumnPickerBase();
    this.rowLimiter = new RowLimiterBase();

    this.users = [];
    this.userRoles = [];

    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;
    this.enableEdit = false;
    this.rowNumber = -1;
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
        sortable: false
      },
      {
        cellTemplate: this.checkBoxTpl,
        name: 'Active',
        canAutoResize: true
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 60,
        canAutoResize: true,
        sortable: false
      }
    ];

    if (this.userService.isDomainVisible()) {
      this.getUserDomains();

      this.columnPicker.allColumns.splice(2, 0,
        {
          cellTemplate: this.editableTpl,
          name: 'Domain',
          prop: 'domain',
          canAutoResize: true
        });
    }
    this.domainService.getCurrentDomain().subscribe((domain: Domain) => this.currentDomain = domain);

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Username', 'Role', 'Domain', 'Password', 'Active', 'Actions'].indexOf(col.name) !== -1
    });

    this.getUsers();

    this.getUserRoles();

    if (this.users.length > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error('Maximum number of rows reached for downloading CSV');
    }

    this.dirty = false;
    this.areRowsDeleted = false;
  }

  getUsers (): void {
    this.userService.getUsers().subscribe(users => this.users = users);
    this.dirty = false;
    this.areRowsDeleted = false;
  }

  getUserRoles (): void {
    this.userService.getUserRoles().subscribe(userroles => this.userRoles = userroles);
  }

  getUserDomains (): void {
    this.domainService.getDomains().subscribe((domains: Domain[]) => this.domains = domains);
  }

  onSelect ({selected}) {
    if (isNullOrUndefined(selected) || selected.length == 0) {
      // unselect
      this.enableDelete = false;
      this.enableEdit = false;

      return;
    }

    // select
    this.rowNumber = this.selected[0].$$index;

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableDelete = selected.length > 0;
    this.enableEdit = selected.length == 1;
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
    this.editedUser = new UserResponseRO('', this.currentDomain.code, '', '', true, UserState[UserState.NEW], [], false);
    this.users.push(this.editedUser);
    this.users = this.users.slice();
    this.rowNumber = this.users.length - 1;
    this.setIsDirty();
    const formRef: MdDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {
      data: {
        edit: false,
        user: this.users[this.rowNumber],
        userroles: this.userRoles,
        userdomains: this.domains
      }
    });
    formRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.users[this.rowNumber].userName = formRef.componentInstance.userName;
        this.onSaveEditForm(formRef);
      } else {
        this.users.pop();
        this.selected = [];
        this.enableEdit = false;
        this.enableDelete = false;
        this.setIsDirty();
      }
    });
  }

  buttonEdit () {
    this.buttonEditAction(this.rowNumber);
  }

  buttonEditAction (rowNumber) {
    const formRef: MdDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {
      data: {
        edit: true,
        user: this.users[rowNumber],
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
    const user = this.users[this.rowNumber];

    user.email = editForm.email;
    user.roles = editForm.roles.toString();
    user.domain = editForm.domain;
    user.password = editForm.password;
    user.active = editForm.active;
    if(editForm.userForm.dirty) {
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
    if (this.isLoggedInUserSelected(this.selected)) {
      this.alertService.error('You cannot delete the logged in user: ' + this.securityService.getCurrentUser().username);
      return;
    }

    this.enableDelete = false;
    this.enableEdit = false;

    // we need to use the old for loop approach to don't mess with the entries on the top before
    for (let i = this.selected.length - 1; i >= 0; i--) {
      this.users.splice(this.selected[i].$$index, 1);
    }

    this.selected = [];
    this.areRowsDeleted = true;
    this.setIsDirty();
  }

  buttonDeleteAction (row) {
    if (this.securityService.getCurrentUser().username === row.userName) {
      this.alertService.error('You cannot delete the logged in user: ' + this.securityService.getCurrentUser().username);
      return;
    }

    this.enableDelete = false;
    this.enableEdit = false;

    // we need to use the old for loop approach to don't mess with the entries on the top before
    this.users.splice(row.$$index, 1);

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

      const headers = new Headers({'Content-Type': 'application/json'});
      this.dialog.open(SaveDialogComponent).afterClosed().subscribe(result => {
        if (result) {
          this.disableSelectionAndButtons();
          this.http.put(UserComponent.USER_USERS_URL, JSON.stringify(this.users), {headers: headers}).subscribe(res => {
            this.getUsers();
            this.getUserRoles();
            this.alertService.success('The operation \'update users\' completed successfully.', false);
            if (withDownloadCSV) {
              DownloadService.downloadNative(UserComponent.USER_CSV_URL);
            }
          }, err => {
            this.getUsers();
            this.getUserRoles();
            this.alertService.exception('The operation \'update users\' not completed successfully.', err, false);
          });
        } else {
          if (withDownloadCSV) {
            DownloadService.downloadNative(UserComponent.USER_CSV_URL);
          }
        }
      });
    } catch (err) {
      this.alertService.exception('The operation \'update users\' completed with errors.', err);
    }
  }


  /**
   * Method that checks if CSV Button export can be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  isSaveAsCSVButtonEnabled (): boolean {
    return this.users.length < AlertComponent.MAX_COUNT_CSV;
  }

  /**
   * Saves the content of the datatable into a CSV file
   */
  saveAsCSV () {
    if (this.isDirty()) {
      this.save(true);
    } else {
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

}
