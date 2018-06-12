import {Component, Inject, ChangeDetectorRef, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';
import {UserValidatorService} from '../../user/uservalidator.service';
import {SecurityService} from '../../security/security.service';
import {Domain} from '../../security/domain';
import {PluginUserRO} from '../pluginuser';

const ROLE_AP_ADMIN = SecurityService.ROLE_AP_ADMIN;
const NEW_MODE = 'New User';
const EDIT_MODE = 'User Edit';

@Component({
  selector: 'editpluginuser-form',
  templateUrl: './editbasicpluginuser-form.component.html',
  styleUrls: ['./editbasicpluginuser-form.component.css'],
  providers: [UserValidatorService]
})
export class EditbasicpluginuserFormComponent {

  existingRoles = [];

  password: any;
  confirmation: any;
  userName = '';
  email = '';
  active = true;
  role: string;

  public emailPattern = '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}';
  public passwordPattern = '^(?=.*[A-Z])(?=.*[ !#$%&\'()*+,-./:;<=>?@\\[^_`{|}~\\\]"])(?=.*[0-9])(?=.*[a-z]).{8,32}$';

  editMode: boolean;

  formTitle: string = EDIT_MODE;

  userForm: FormGroup;

  user: PluginUserRO;

  constructor (public dialogRef: MdDialogRef<EditbasicpluginuserFormComponent>,
               @Inject(MD_DIALOG_DATA) public data: any,
               fb: FormBuilder,
               userValidatorService: UserValidatorService,
               private securityService: SecurityService,
               private cdr: ChangeDetectorRef) {

    this.existingRoles = data.userroles;

    this.editMode = data.edit;

    this.user = data.user;
    this.confirmation = data.user.password;

    if (this.editMode) {
      this.userForm = fb.group({
        'userName': new FormControl({value: this.user.username, disabled: false}, Validators.nullValidator),
        'originalUser': new FormControl(this.user.originalUser, Validators.nullValidator),
        'role': new FormControl(this.user.authRoles, Validators.required),
        'password': [null, Validators.pattern],
        'confirmation': [null],
      }, {
        validator: userValidatorService.validateForm()
      });
    } else {
      this.formTitle = NEW_MODE;
      this.userForm = fb.group({
        'userName': new FormControl(this.userName, Validators.required),
        'originalUser': new FormControl(this.user.originalUser, Validators.nullValidator),
        'roles': new FormControl(this.role, Validators.required),
        'password': [Validators.required, Validators.pattern],
        'confirmation': [Validators.required],
      }, {
        validator: userValidatorService.validateForm()
      });
    }
  }

  // updateUserName (event) {
  //   this.user.username = event.target.value;
  // }
  //
  // updatePassword (event) {
  //   this.user.password = event.target.value;
  // }
  //
  // updateConfirmation (event) {
  //   this.confirmation = event.target.value;
  // }

  // updateActive (event) {
  //   this.active = event.target.checked;
  // }

  submitForm () {
    this.dialogRef.close(true);
  }

}
