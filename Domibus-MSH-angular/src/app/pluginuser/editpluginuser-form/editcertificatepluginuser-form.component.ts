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
  selector: 'editcertificatepluginuser-form',
  templateUrl: './editcertificatepluginuser-form.component.html',
  providers: [UserValidatorService]
})
export class EditcertificatepluginuserFormComponent {

  existingRoles = [];
  editMode: boolean;
  formTitle: string;
  userForm: FormGroup;
  user: PluginUserRO;

  public originalUserPattern = PluginUserService.originalUserPattern;
  public originalUserMessage = PluginUserService.originalUserMessage;

  public certificateIdPattern = PluginUserService.certificateIdPattern;
  public certificateIdMessage = PluginUserService.certificateIdMessage;

  constructor (public dialogRef: MdDialogRef<EditcertificatepluginuserFormComponent>,
               @Inject(MD_DIALOG_DATA) public data: any,
               fb: FormBuilder) {

    this.existingRoles = data.userroles;
    this.editMode = data.edit;
    this.user = data.user;

    this.formTitle = this.editMode ? EDIT_MODE : NEW_MODE;

    if (this.editMode) {
      this.userForm = fb.group({
        'certificateId': new FormControl({value: this.user.certificateId, disabled: true}, Validators.nullValidator),
        'originalUser': new FormControl(this.user.originalUser, Validators.nullValidator),
        'role': new FormControl(this.user.authRoles, Validators.required),
      });
    } else {
      this.userForm = fb.group({
        'certificateId': new FormControl(this.user.certificateId, Validators.required),
        'originalUser': new FormControl(this.user.originalUser, Validators.nullValidator),
        'role': new FormControl(this.user.authRoles, Validators.required),
      });
    }
  }

  submitForm () {
    this.dialogRef.close(true);
  }

}
