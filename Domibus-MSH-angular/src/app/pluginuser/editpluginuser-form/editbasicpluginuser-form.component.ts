import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';
import {UserValidatorService} from '../../user/uservalidator.service';
import {PluginUserRO} from '../pluginuser';
import {PluginUserService} from '../pluginuser.service';
import {SecurityService} from '../../security/security.service';

const NEW_MODE = 'New Plugin User';
const EDIT_MODE = 'Plugin User Edit';

@Component({
  selector: 'editbasicpluginuser-form',
  templateUrl: './editbasicpluginuser-form.component.html',
  providers: [UserValidatorService]
})

export class EditbasicpluginuserFormComponent implements OnInit {

  existingRoles = [];
  confirmation: string;
  public passwordPattern: string;
  public passwordValidationMessage: string;
  editMode: boolean;
  formTitle: string;
  userForm: FormGroup;
  user: PluginUserRO;

  public originalUserPattern = PluginUserService.originalUserPattern;
  public originalUserMessage = PluginUserService.originalUserMessage;

  public certificateIdPattern = PluginUserService.certificateIdPattern;
  public certificateIdMessage = PluginUserService.certificateIdMessage;

  constructor(public dialogRef: MdDialogRef<EditbasicpluginuserFormComponent>,
              @Inject(MD_DIALOG_DATA) public data: any,
              fb: FormBuilder,
              private securityService: SecurityService,
              userValidatorService: UserValidatorService) {

    this.existingRoles = data.userroles;
    this.editMode = data.edit;
    this.user = data.user;

    this.confirmation = data.user.password;

    this.formTitle = this.editMode ? EDIT_MODE : NEW_MODE;

    if (this.editMode) {
      this.userForm = fb.group({
        'userName': new FormControl({value: this.user.userName, disabled: true}, Validators.nullValidator),
        'originalUser': new FormControl(this.user.originalUser, null),
        'role': new FormControl(this.user.authRoles, Validators.required),
        'password': [null],
        'confirmation': [null],
        'active': new FormControl(this.user.active, Validators.required)
      }, {
        validator: userValidatorService.validateForm()
      });
    } else {
      this.userForm = fb.group({
        'userName': new FormControl(this.user.userName, Validators.required),
        'originalUser': new FormControl(this.user.originalUser, null),
        'role': new FormControl(this.user.authRoles, Validators.required),
        'password': [Validators.required, Validators.pattern],
        'confirmation': [Validators.required],
        'active': [Validators.required]
      }, {
        validator: userValidatorService.validateForm()
      });
    }
  }

  async ngOnInit() {
    const passwordPolicy = await this.securityService.getPluginPasswordPolicy();
    this.passwordPattern = passwordPolicy.pattern;
    this.passwordValidationMessage = passwordPolicy.validationMessage;
  }

  updateActive (event) {
    this.user.active = event.target.checked;
  }

  submitForm() {
    this.dialogRef.close(true);
  }

}
