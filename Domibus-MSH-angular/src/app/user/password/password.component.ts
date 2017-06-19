import { Component, OnInit } from '@angular/core';
import {User} from "../user";
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-password',
  templateUrl: './password.component.html',
  styleUrls: ['./password.component.css']
})
export class PasswordComponent implements OnInit {

  editedUser:User;
  passwordVerification:any;
  passwordVerificationLabel:String;
  constructor(public dialogRef: MdDialogRef<PasswordComponent>) {
    this.passwordVerificationLabel="Confirm password"
  }

  ngOnInit() {
  }

  save():void{
    console.log("Edited "+this.editedUser.password+" "+this.passwordVerification)
    if(this.editedUser.password===this.passwordVerification){
      this.dialogRef.close();
    }else{
      this.passwordVerificationLabel="Incorrect password confirmation";
      this.passwordVerification="";
    }
  }
  cancel():void{
    this.dialogRef.close();
  }

}
