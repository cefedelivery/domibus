import {Component, NgZone, OnInit} from "@angular/core";
import {UserResponseRO, UserState} from "./user";
import {UserService} from "./user.service";
import {MdDialog, MdDialogRef} from "@angular/material";
import {PasswordComponent} from "./password/password-dialog.component";
import {UserValidatorService} from "app/user/uservalidator.service";
import {AlertService} from "../alert/alert.service";
import {MessagefilterDialogComponent} from "app/messagefilter/messagefilter-dialog/messagefilter-dialog.component";
import {CancelMessagefilterDialogComponent} from "../messagefilter/cancelmessagefilter-dialog/cancelmessagefilter-dialog.component";


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
  userSaveButtonDisabled = false;
  userNewButtonDisabled = false;
  userCancelButtonDisabled = false;
  editedUser: UserResponseRO;
  selected = [];
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

  singleSelectCheck(row: any) {
    return this.selected.indexOf(row) === -1;
  }


}
