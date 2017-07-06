/**
 * @author Thomas Dussart
 * @since 3.3
 */

import {Component, Inject} from '@angular/core';
import {UserResponseRO, UserState} from "../user";
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

  submitForm(form: any): void{
    this.data.password=form['password'];
    if(UserState[UserState.PERSISTED]===this.data.status) {
      this.data.status = UserState[UserState.UPDATED]
    }
    this.dialogRef.close(true);
  }

}
