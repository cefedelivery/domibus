import {Component, OnInit} from "@angular/core";
import {UserResponseRO, UserState} from "./user";
import {UserService} from "./user.service";
import {MdDialog, MdDialogRef} from "@angular/material";
import {UserValidatorService} from "app/user/uservalidator.service";
import {AlertService} from "../alert/alert.service";
import {MessagefilterDialogComponent} from "app/common/save-dialog/save-dialog.component";
import {EditUserComponent} from "app/user/edituser-form/edituser-form.component";
import {isNullOrUndefined} from "util";
import {Http, Headers} from "@angular/http";
import {DirtyOperations} from "../common/dirty-operations";
import {CancelDialogComponent} from "../common/cancel-dialog/cancel-dialog.component";


@Component({
  moduleId: module.id,
  templateUrl: 'user.component.html',
  providers: [UserService, UserValidatorService],
  styleUrls: ['./user.component.css']
})


export class UserComponent implements OnInit ,DirtyOperations{

  users: Array<UserResponseRO> = [];
  userRoles: Array<String> = [];
  pageSize: number = 10;

  selected = [];

  enableCancel = false;
  enableSave = false;
  enableDelete = false;
  enableEdit = false;

  rowNumber = -1;

  editedUser: UserResponseRO;
  test: boolean = false;

  constructor(private http: Http, private userService: UserService, public dialog: MdDialog, private userValidatorService: UserValidatorService, private alertService: AlertService) {
  }

  ngOnInit(): void {
    this.getUsers();
    this.getUserRoles();
  }

  getUsers(): void {
    this.userService.getUsers().subscribe(users => this.users = users);
  }

  getUserRoles() : void {
    this.userService.getUserRoles().subscribe( userroles => this.userRoles = userroles);
  }

  onSelect({selected}) {
    console.log('Select Event', selected, this.selected);

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

  buttonNew(): void {
    this.editedUser = new UserResponseRO("", "", "", true, UserState[UserState.NEW], [""]);
    this.users.push(this.editedUser);
    this.users = this.users.slice();
    this.rowNumber = this.users.length - 1;
    let formRef: MdDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {data: {edit: false, user: this.users[this.rowNumber], userroles: this.userRoles}});
    formRef.afterClosed().subscribe(result => {
      if(result == true) {
        this.updateUsername(formRef.componentInstance.userName);
        this.updateEmail(formRef.componentInstance.email);
        this.updateRoles(formRef.componentInstance.roles.toString());
        this.updatePassword(formRef.componentInstance.password);
        this.updateActive(formRef.componentInstance.active);
        if(UserState[UserState.PERSISTED]===this.users[this.rowNumber].status) {
          this.users[this.rowNumber].status = UserState[UserState.UPDATED]
        }

        this.enableSave = true;
        this.enableCancel = true;
      } else {
        this.users.pop();
      }
    });
  }

  buttonEdit() {
    let formRef: MdDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {data: {edit: true, user: this.users[this.rowNumber], userroles: this.userRoles}});
    formRef.afterClosed().subscribe(result => {
      if(result == true) {
        //this.updateUsername(formRef.componentInstance.userName);
        this.updateEmail(formRef.componentInstance.email);
        this.updateRoles(formRef.componentInstance.roles.toString());
        this.updatePassword(formRef.componentInstance.password);
        this.updateActive(formRef.componentInstance.active);
        if(UserState[UserState.PERSISTED]===this.users[this.rowNumber].status) {
          this.users[this.rowNumber].status = UserState[UserState.UPDATED]
        }

        this.enableSave = true;
        this.enableCancel = true;
      }
    });
  }

  private updateUsername(value: string) {
    this.users[this.rowNumber].userName = value;
  }

  private updateEmail(value: string) {
    this.users[this.rowNumber].email = value;
  }

  private updateRoles(value: string) {
    this.users[this.rowNumber].roles = value;
  }

  private updatePassword(value: string) {
    this.users[this.rowNumber].password = value;
  }

  private updateActive(value: boolean) {
    this.users[this.rowNumber].active = value;
  }

  buttonDelete() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;
    this.enableEdit = false;

    // we need to use the old for loop approach to don't mess with the entries on the top before
    for (let i = this.selected.length - 1; i >= 0; i--) {
      this.users.splice(this.selected[i].$$index, 1);
    }

    this.selected = [];
  }

  private disableSelectionAndButtons() {
    this.selected = [];
    this.enableCancel = false;
    this.enableSave = false;
    this.enableEdit = false;
    this.enableDelete = false;
  }

  cancelDialog() {
    let dialogRef = this.dialog.open(CancelDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch (result) {
        case 'Yes' :
          this.disableSelectionAndButtons();
          this.getUsers();

          break;
        case 'No':
        // do nothing
      }
    });
  }

  saveDialog() {
    let headers = new Headers({'Content-Type': 'application/json'});
    let dialogRef = this.dialog.open(MessagefilterDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch (result) {
        case 'Save' :
          this.disableSelectionAndButtons();
          this.http.put('rest/user/users', JSON.stringify(this.users), {headers: headers}).subscribe(res => {
            this.getUsers();
            this.getUserRoles();
            this.alertService.success("The operation 'update users' completed successfully.", false);
          }, err => {
            this.getUsers();
            this.getUserRoles();
            this.alertService.error("The operation 'update users' not completed successfully.", false);
          });
          break;
        case 'Cancel':
        // do nothing
      }
    });
  }

  isDirty(): boolean {
    return this.enableCancel;
  }

}
