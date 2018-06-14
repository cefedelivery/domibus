import {Component, Inject, ChangeDetectorRef, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';
import {UserValidatorService} from '../../user/uservalidator.service';
import {SecurityService} from '../../security/security.service';
import {PluginUserRO} from '../pluginuser';
import {PluginUserService} from '../pluginuser.service';

const NEW_MODE = 'New User';
const EDIT_MODE = 'User Edit';

@Component({
  selector: 'editbasicpluginuser-form',
  templateUrl: './editbasicpluginuser-form.component.html',
  providers: [UserValidatorService]
})
export class EditbasicpluginuserFormComponent {

  existingRoles = [];
  confirmation: string;
  public passwordPattern = PluginUserService.passwordPattern;
  editMode: boolean;
  formTitle: string;
  userForm: FormGroup;
  user: PluginUserRO;

  public originalUserPattern = PluginUserService.originalUserPattern;
  public originalUserMessage = PluginUserService.originalUserMessage;

  public certificateIdPattern = PluginUserService.certificateIdPattern;
  public certificateIdMessage = PluginUserService.certificateIdMessage;

  constructor (public dialogRef: MdDialogRef<EditbasicpluginuserFormComponent>,
               @Inject(MD_DIALOG_DATA) public data: any,
               fb: FormBuilder,
               userValidatorService: UserValidatorService) {

    this.existingRoles = data.userroles;
    this.editMode = data.edit;
    this.user = data.user;

    this.confirmation = data.user.password;

    this.formTitle = this.editMode ? EDIT_MODE : NEW_MODE;

    if (this.editMode) {
      this.userForm = fb.group({
        'userName': new FormControl({value: this.user.username, disabled: true}, Validators.nullValidator),
        'originalUser': new FormControl(this.user.originalUser, Validators.nullValidator),
        'role': new FormControl(this.user.authRoles, Validators.required),
        'password': [null, Validators.pattern],
        'confirmation': [null],
      }, {
        validator: userValidatorService.validateForm()
      });
    } else {
      this.userForm = fb.group({
        'userName': new FormControl(this.user.username, Validators.required),
        'originalUser': new FormControl(this.user.originalUser, Validators.nullValidator),
        'role': new FormControl(this.user.authRoles, Validators.required),
        'password': [Validators.required, Validators.pattern],
        'confirmation': [Validators.required],
      }, {
        validator: userValidatorService.validateForm()
      });
    }
  }

  submitForm () {
    this.dialogRef.close(true);
  }

}
