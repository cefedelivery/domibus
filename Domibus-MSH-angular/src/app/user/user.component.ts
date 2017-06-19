import {Component, NgZone, OnInit} from "@angular/core";
import {User, UserState} from "./user";
import {UserService} from "./user.service";
import {MdDialog, MdDialogRef} from "@angular/material";
import {PasswordComponent} from "./password/password.component";


@Component({
  moduleId: module.id,
  templateUrl: 'user.component.html',
  providers: [UserService],
  styleUrls: ['./user.component.css']
})


export class UserComponent implements OnInit {
  users: Array<User>;
  pageSize: number = 10;
  editing = {};
  zone: NgZone;
  userSaveButtonDisabled=true;
  userNewButtonDisabled=false;
  userCancelButtonDisabled=true;
  editedUser:User ;


  constructor(private userService: UserService,public dialog: MdDialog) {
    this.zone = new NgZone({enableLongStackTrace: false});
  }

  ngOnInit(): void {
    this.getUsers();
  }

  getUsers(): void {
    this.userService.getUsers().then(users => this.users = users);
  }

  onSelect({selected}) {
    //this.clearEditing();

  }

  onActivate(event) {

  }

  updateValue(event, cell, row) {
    //this.clearEditing();
    //this.editing[row.$$index + '-' + cell] = false;
    this.users[row.$$index][cell] = event.target.value;
  }

    clearEditing():void{
      for (let edit in this.editing) {
        debugger;
        console.log(edit);
        console.log(this.editing[edit]);
        this.editing[edit]=false;
      }
      this.users=this.users.slice();
  }

  newUser(): void {
    this.zone.run(() => {
      this.clearEditing();
      this.editedUser = new User("", "", "", true,UserState.NEW);
      let userCount = this.userService.addUser(this.editedUser);
      console.log('usecount '+userCount);
      this.editing[userCount-1 + '-' + 'userName'] = true;
      this.editing[userCount-1 + '-' + 'email'] = true;
      this.userCancelButtonDisabled=false;
      this.userNewButtonDisabled=true;
    });

    this.getUsers();

  }
  focus(){
    if(this.editedUser.isNew()){
      console.log("Setting new password");
      let dialogRef: MdDialogRef<PasswordComponent> = this.dialog.open(PasswordComponent);
      dialogRef.componentInstance.editedUser = this.editedUser;
    }

  }


}
