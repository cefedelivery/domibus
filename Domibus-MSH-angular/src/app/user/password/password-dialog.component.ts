import {Component, Inject, OnInit} from '@angular/core';
import {UserResponseRO} from "../user";
import {MdDialogRef} from "@angular/material";
import {MD_DIALOG_DATA} from '@angular/material';

@Component({
  selector: 'app-password',
  templateUrl: './password-dialog.component.html',
  styleUrls: ['./password-dialog.component.css']
})
export class PasswordComponent implements OnInit {

  passwordVerification:any;
  passwordError:boolean;
  constructor(public dialogRef: MdDialogRef<PasswordComponent>,@Inject(MD_DIALOG_DATA) public data: UserResponseRO) {
  }

  ngOnInit() {
  }

  save():void{
    console.log("Edited "+this.data.password+" "+this.passwordVerification)
    if(this.data.password===this.passwordVerification){
      this.dialogRef.close(true);
    }else{
      this.passwordError=true;
      this.passwordVerification="";
    }
  }
  cancel():void{
    this.dialogRef.close(false);
  }

}
