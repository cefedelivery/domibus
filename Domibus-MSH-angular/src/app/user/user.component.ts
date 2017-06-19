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

  updateEmail(event, row){
    console.log("Update email")
    this.updateValue(event,"email",row);
    this.emailFocusOut();

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
      this.users.push(this.editedUser);
      this.users=this.users.slice();
      let userCount = this.users.length;
      console.log('usecount '+userCount);
      this.editing[userCount-1 + '-' + 'userName'] = true;
      this.editing[userCount-1 + '-' + 'email'] = true;
      this.userCancelButtonDisabled=false;
      this.userNewButtonDisabled=true;
    });

    //this.getUsers();

  }
  emailFocusOut(){
    debugger;
    console.log("Focus out ");
    console.log("User is new "+this.editedUser.isNew());
    if(this.editedUser.isNew()){
      console.log("Setting new password");
      let dialogRef: MdDialogRef<PasswordComponent> = this.dialog.open(PasswordComponent);
      dialogRef.componentInstance.editedUser = this.editedUser;
    }
  }
  cancel(){
    this.getUsers();
    this.users=this.users.slice();
    this.userCancelButtonDisabled=true;
    this.userNewButtonDisabled=false;
  }


}
