import {Component, NgZone, OnInit} from "@angular/core";
import {UserResponseRO, UserState} from "./user";
import {UserService} from "./user.service";
import {MdDialog, MdDialogRef} from "@angular/material";
import {PasswordComponent} from "./password/password-dialog.component";
import {UserValidatorService} from "app/user/uservalidator.service";
import {AlertService} from "../alert/alert.service";
import {MessagefilterDialogComponent} from "app/messagefilter/messagefilter-dialog/messagefilter-dialog.component";
import {CancelMessagefilterDialogComponent} from "../messagefilter/cancelmessagefilter-dialog/cancelmessagefilter-dialog.component";
import {EditUserComponent} from "app/user/edituser-form/edituser-form.component";
import {isNullOrUndefined} from "util";


@Component({
  moduleId: module.id,
  templateUrl: 'user.component.html',
  providers: [UserService, UserValidatorService],
  styleUrls: ['./user.component.css']
})


export class UserComponent implements OnInit {
  users: Array<UserResponseRO> = [];
  pageSize: number = 10;
  editing = {};
  zone: NgZone;

  selected = [];

  enableCancel = false;
  enableSave = false;
  enableDelete = false;
  enableEdit = false;

  rowNumber = -1;

  editedUser: UserResponseRO;
  test: boolean = false;

  constructor(private userService: UserService, public dialog: MdDialog, private userValidatorService: UserValidatorService, private alertService: AlertService) {
    this.zone = new NgZone({enableLongStackTrace: false});
  }

  ngOnInit(): void {
    this.getUsers();
  }

  getUsers(): void {
    this.userService.getUsers().subscribe(users => this.users = users);
  }

  onSelect({selected}) {
    console.log('Select Event', selected, this.selected);

    if (isNullOrUndefined(selected) || selected.length == 0) {
      // unselect
      this.enableDelete = false;
      this.enableEdit = false;

      return;
    }

    this.rowNumber = this.selected[0].$$index;

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableDelete = selected.length > 0;
    this.enableEdit = selected.length == 1;
  }

  updateValue(event, cell, row) {
    //@thom check if this zone is really needed.
    this.zone.run(() => {
      this.clearEditing();
      if (this.users[row.$$index][cell] !== event.target.value) {
        this.users[row.$$index][cell] = event.target.value
        if (UserState[UserState.NEW] != this.users[row.$$index].status) {
          this.users[row.$$index].status = UserState[UserState.UPDATED]
        }
      }
    });
  }

  updateCheckBox(event, cell, row) {
    let checked: boolean = event.srcElement.checked;
    this.users[row.$$index][cell] = checked;
    if (UserState[UserState.NEW] != this.users[row.$$index].status) {
      this.users[row.$$index].status = UserState[UserState.UPDATED]
    }
  }

  clearEditing(): void {
    for (let edit in this.editing) {
      this.editing[edit] = false;
    }
    this.users = this.users.slice();
  }

  newUser(): void {
    this.zone.run(() => {
      this.clearEditing();
      this.editedUser = new UserResponseRO("", "", "", true, UserState[UserState.NEW], [""]);
      this.users.push(this.editedUser);
      this.users = this.users.slice();
      let userCount = this.users.length;
      console.log('usecount ' + userCount);
      this.editing[userCount - 1 + '-' + 'userName'] = true;
    });
  }

  buttonNew() {
    let formRef: MdDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {});
    formRef.afterClosed().subscribe(result => {
      if(result == true) {

        this.enableSave = true;
        this.enableCancel = true;
      }
    });
  }

  buttonEdit() {
    let formRef: MdDialogRef<EditUserComponent> = this.dialog.open(EditUserComponent, {data: this.users[this.rowNumber]});
    formRef.afterClosed().subscribe(result => {
      if(result == true) {

        this.enableSave = true;
        this.enableCancel = true;
      }
    });
  }

  openPasswordDialog(rowIndex) {
    let dialogRef: MdDialogRef<PasswordComponent> = this.dialog.open(PasswordComponent, {data: this.users[rowIndex]});
    dialogRef.afterClosed().subscribe(result => {
      if (result == true) {
        this.zone.run(() => {
          this.clearEditing();
          this.users = this.users.slice();
          this.alertService.clearAlert();
        });
      }
    });
  }

  cancel() {
    let filteredUsers = this.filterModifiedUser();
    if (filteredUsers.length > 0) {
      let dialogRef: MdDialogRef<CancelMessagefilterDialogComponent> = this.dialog.open(CancelMessagefilterDialogComponent);
      dialogRef.afterClosed().subscribe(result => {
        if (result === "Yes") {
          this.users = [];
          this.getUsers();
        }
      });
    }
  }

  deleteUser() {

  }

  filterModifiedUser(): UserResponseRO[] {
    return this.users.filter(user => user.status !== UserState[UserState.PERSISTED]);
  }

  userNameDblClick(number) {
    console.log(number);
    if (this.users[number].status == UserState[UserState.NEW]) {
      this.editing[number + '-userName'] = true
    }
  }

  save() {
    let filteredUsers = this.filterModifiedUser();
    if (filteredUsers.length > 0) {
      if (this.userValidatorService.validateUsers(filteredUsers, this.users)) {
        let dialogRef: MdDialogRef<MessagefilterDialogComponent> = this.dialog.open(MessagefilterDialogComponent);
        dialogRef.afterClosed().subscribe(result => {
          if (result === "Save") {
            for (let u in filteredUsers) {
              let user: UserResponseRO = filteredUsers[u];
              user.authorities = user.roles.split(",");
            }
            this.userService.saveUsers(filteredUsers);
          }
        });
      }
    }
  }

}
