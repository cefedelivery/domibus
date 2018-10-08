import {Component, Inject, ChangeDetectorRef, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';
import {UserValidatorService} from '../user/uservalidator.service';
import {SecurityService} from '../security/security.service';
import {PluginUserService} from '../pluginuser/pluginuser.service';

@Component({
  selector: 'editbasicpluginuser-form',
  templateUrl: './changePassword.component.html',
  providers: [UserValidatorService]
})
export class ChangePasswordComponent {

  confirmation: string;
  public passwordPattern = PluginUserService.passwordPattern;
  formTitle: string;
  userForm: FormGroup;

  public originalUserPattern = PluginUserService.originalUserPattern;
  public originalUserMessage = PluginUserService.originalUserMessage;

  constructor (public dialogRef: MdDialogRef<ChangePasswordComponent>,
               @Inject(MD_DIALOG_DATA) public data: any,
               fb: FormBuilder,
               userValidatorService: UserValidatorService) {

    this.userForm = fb.group({
      'password': [Validators.required, Validators.pattern],
      'confirmation': [Validators.required],
    }, {
      validator: userValidatorService.validateForm()
    });
  }

  submitForm () {
    this.dialogRef.close(true);
  }

}
