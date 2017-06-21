import {Component, NgZone, OnInit} from "@angular/core";
import {UserResponseRO, UserState} from "./user";
import {UserService} from "./user.service";
import {MdDialog, MdDialogRef} from "@angular/material";
import {PasswordComponent} from "./password/password-dialog.component";
import {UserValidatorService} from "app/user/uservalidator.service";


@Component({
  moduleId: module.id,
  templateUrl: 'user.component.html',
  providers: [UserService,UserValidatorService],
  styleUrls: ['./user.component.css']
})


export class UserComponent implements OnInit {
  users: Array<UserResponseRO> = [];
  pageSize: number = 10;
  editing = {};
  zone: NgZone;
  userSaveButtonDisabled = true;
  userNewButtonDisabled = false;
  userCancelButtonDisabled = true;
  editedUser: UserResponseRO;
  selected = [];
  test: boolean = false;

  constructor(private userService: UserService, public dialog: MdDialog,private userValidatorService:UserValidatorService) {
    this.zone = new NgZone({enableLongStackTrace: false});
  }

  ngOnInit(): void {
    this.getUsers();
  }

  getUsers(): void {
    this.userService.getUsers().subscribe(users => this.users = users);
    /*for(let u in users){
     this.users.push(new UserResponseRO(users[u]["userName"],users[u]["email"],"",users[u]["active"],UserState.PERSISTED,users[u]["authorities"]));
     }
     });*/
    this.users = this.users.slice();

  }

  onSelect(selected) {
    console.log("Selected " + selected);
    //this.clearEditing();

  }

  onActivate(event) {

  }

  /*updateEmail(event, row) {
    console.log("Update email")
    this.updateValue(event, "email", row);
    this.emailFocusOut();

  }*/

  updateValue(event, cell, row) {
    this.zone.run(() => {
      this.clearEditing();
      //this.editing[row.$$index + '-' + cell] = false;
      this.users[row.$$index][cell] = event.target.value;
    });

  }

  clearEditing(): void {
    for (let edit in this.editing) {
      console.log(edit);
      console.log(this.editing[edit]);
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
      //    this.editing[userCount-1 + '-' + 'email'] = true;
      this.userCancelButtonDisabled = false;
      this.userNewButtonDisabled = true;
    });

    //this.getUsers();

  }

  openPasswordDialog(rowIndex) {
    let dialogRef: MdDialogRef<PasswordComponent> = this.dialog.open(PasswordComponent, {data: this.users[rowIndex]});
    dialogRef.afterClosed().subscribe(result => {
      console.log("Dialog result " + result)

      if (result == true) {
        this.zone.run(() => {
          this.clearEditing();
          this.userCancelButtonDisabled = false;
          this.userNewButtonDisabled = false;
          this.userSaveButtonDisabled = false;
          this.users = this.users.slice();
        });
      } else {

      }
    });
  }

  /*emailFocusOut() {
    console.log("Focus out");
    console.log("User is new " + this.editedUser.isNew());
    if (this.editedUser.isNew() && this.editedUser.password === "") {
      console.log("Setting new password");
      let dialogRef: MdDialogRef<PasswordComponent> = this.dialog.open(PasswordComponent, {data: this.editedUser});
      dialogRef.afterClosed().subscribe(result => {
        console.log("Dialog result " + result)

        if (result == true) {
          this.zone.run(() => {
            this.clearEditing();
            this.userCancelButtonDisabled = false;
            this.userNewButtonDisabled = false;
            this.userSaveButtonDisabled = false;
            this.users = this.users.slice();
          });
        } else {

        }
      });

    }
  }*/

  cancel() {
    this.users = [];
    this.getUsers();
    //this.users=this.users.slice();
    this.userCancelButtonDisabled = true;
    this.userNewButtonDisabled = false;
  }


  filterModifiedUser():UserResponseRO[]{
    return this.users.filter(user=>user.status!==UserState[UserState.PERSISTED]);
  }

  save(){
    if(this.userValidatorService.validateNewUsers(this.filterModifiedUser(),this.users)) {
      let filteredUsers = this.filterModifiedUser();
      for(let u in filteredUsers){
        let user:UserResponseRO=filteredUsers[u];
        user.authorities=user.roles.split(",");
      }
      this.userService.saveUsers(filteredUsers);
    }
  }

  /*shouldEditUserName(row): boolean {
    return this.users[row].isNew();

  }*/


}
