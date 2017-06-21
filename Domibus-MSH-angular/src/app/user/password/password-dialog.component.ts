import {Component, Inject, OnInit} from '@angular/core';
import {UserResponseRO} from "../user";
import {MdDialogRef} from "@angular/material";
import {MD_DIALOG_DATA} from '@angular/material';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserValidatorService} from "../uservalidator.service";

@Component({
  selector: 'app-password',
  templateUrl: './password-dialog.component.html',
  styleUrls: ['./password-dialog.component.css'],
  providers:[UserValidatorService]
})
export class PasswordComponent {

  password:any;
  passwordVerification:any;
  passwordError:boolean;
  passwordForm:FormGroup;
  constructor(public dialogRef: MdDialogRef<PasswordComponent>,
              @Inject(MD_DIALOG_DATA) public data: UserResponseRO,
              fb: FormBuilder,
              userValidatorService:UserValidatorService) {
    this.passwordForm=fb.group({
        'password':[null,Validators.compose([Validators.required, Validators.minLength(8), Validators.maxLength(32)])],
        'passwordConfirmation':[null,Validators.required]
      },{
       validator:userValidatorService.matchPassword
      }
    );
  }



  save():void{
    console.log("Edited "+this.data.password+" "+this.passwordVerification)
    if(this.data.password===this.passwordVerification){

    }else{
      this.passwordError=true;
      this.passwordVerification="";
    }
  }
  cancel():void{
    this.dialogRef.close(false);
  }

  submitForm(form: any): void{
    console.log('Form Data: ');
    console.log(form);
    this.data.password=form['password'];
    this.dialogRef.close(true);
  }

}
