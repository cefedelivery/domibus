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
import {Http, Headers} from "@angular/http";


@Component({
  moduleId: module.id,
  templateUrl: 'user.component.html',
  providers: [UserService, UserValidatorService],
  styleUrls: ['./user.component.css']
})


export class UserComponent implements OnInit {
  users: Array<UserResponseRO> = [];
  userRoles: Array<String> = [];
  pageSize: number = 10;
  editing = {};
  //zone: NgZone;

  selected = [];

  enableCancel = false;
  enableSave = false;
  enableDelete = false;
  enableEdit = false;

  rowNumber = -1;

  editedUser: UserResponseRO;
  test: boolean = false;

  constructor(private http: Http, private userService: UserService, public dialog: MdDialog, private userValidatorService: UserValidatorService, private alertService: AlertService) {
    //this.zone = new NgZone({enableLongStackTrace: false});
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

  /*updateValue(event, cell, row) {
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
  }*/

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

  /*openPasswordDialog(rowIndex) {
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
  }*/

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

  buttonDelete() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;
    this.enableEdit = false;

    // we need to use the old for loop approach to don't mess with the entries on the top before
    for (let i = this.selected.length - 1; i >= 0; i--) {
      //this.users.splice(this.selected[i].$$index, 1);
      this.selected[i].status = UserState[UserState.DELETE];
    }

    this.selected = [];

  }

  filterModifiedUser(): UserResponseRO[] {
    return this.users.filter(user => (user.status !== UserState[UserState.PERSISTED] && user.status !== UserState[UserState.DELETE]));
  }

  filterDeletedUsers(): UserResponseRO[] {
    return this.users.filter(user => user.status === UserState[UserState.DELETE]);
  }

  userNameDblClick(number) {
    console.log(number);
    if (this.users[number].status == UserState[UserState.NEW]) {
      this.editing[number + '-userName'] = true
    }
  }

  /*save() {
    let headers = new Headers({'Content-Type': 'application/json'});
    let dialogRef = this.dialog.open(MessagefilterDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch (result) {
        case 'Save' :
          this.http.put('rest/user/save', JSON.stringify(this.users), {headers: headers}).subscribe(res => {
            this.alertService.success("The operation 'update message filters' completed successfully.", false);

          }, err => {
            this.alertService.error("The operation 'update message filters' not completed successfully.", false);
          });
          break;
      }
    });
  }*/

  save() {
    let filteredDeletedUsers = this.filterDeletedUsers();
    // delete
    if (filteredDeletedUsers.length > 0) {
      this.userService.deleteUsers(filteredDeletedUsers);
      this.enableSave = false;
      this.enableCancel = false;
    }

    let filteredModifiedUsers = this.filterModifiedUser();
    // edit or new
    if (filteredModifiedUsers.length > 0) {
      if (this.userValidatorService.validateUsers(filteredModifiedUsers, this.users)) {
        let dialogRef: MdDialogRef<MessagefilterDialogComponent> = this.dialog.open(MessagefilterDialogComponent);
        dialogRef.afterClosed().subscribe(result => {
          if (result === "Save") {
            for (let u in filteredModifiedUsers) {
              let user: UserResponseRO = filteredModifiedUsers[u];
              user.authorities = user.roles.split(",");
            }
            this.userService.saveUsers(filteredModifiedUsers);

            this.enableSave = false;
            this.enableCancel = false;
          }
        });
      }
    }
    /*else {
           // delete
           let dialogRef: MdDialogRef<MessagefilterDialogComponent> = this.dialog.open(MessagefilterDialogComponent);
           dialogRef.afterClosed().subscribe(result => {
             if (result === "Save") {
               //this.userService.saveUsers(this.users);

               this.enableSave = false;
               this.enableCancel = false;
             }
           });
         }*/
  }
}
