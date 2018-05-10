import {Component, Inject, ChangeDetectorRef} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';
import {UserValidatorService} from '../uservalidator.service';
import {SecurityService} from '../../security/security.service';
import {SettingsService} from '../../security/settings.service';

const ROLE_AP_ADMIN = 'ROLE_AP_ADMIN';
const NEW_MODE = 'New User';
const EDIT_MODE = 'User Edit';

@Component({
  selector: 'edituser-form',
  templateUrl: 'edituser-form.component.html',
  providers: [UserValidatorService]
})

export class EditUserComponent {

  public static ROLE_AP_ADMIN = ROLE_AP_ADMIN;

  existingRoles = [];
  existingDomains = [];

  password: any;
  confirmation: any;
  userName = '';
  email = '';
  active = true;
  roles: string[] = [];
  oldRoles = [];
  domain: string;

  public emailPattern = '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}';
  public passwordPattern = '^(?=.*[A-Z])(?=.*[ !#$%&\'()*+,-./:;<=>?@\\[^_`{|}~\\\]"])(?=.*[0-9])(?=.*[a-z]).{8,32}$';

  editMode: boolean;
  multiDomain: boolean;
  isDomainDisabled = false;

  formTitle: string = EDIT_MODE;

  userForm: FormGroup;

  constructor (public dialogRef: MdDialogRef<EditUserComponent>,
               @Inject(MD_DIALOG_DATA) public data: any,
               fb: FormBuilder,
               userValidatorService: UserValidatorService,
               private securityService: SecurityService,
               private settingsService: SettingsService,
               private cdr: ChangeDetectorRef) {

    this.multiDomain = this.settingsService.isMultiDomain();
    this.existingRoles = data.userroles;
    this.existingDomains = data.userdomains;

    this.editMode = data.edit;
    this.userName = data.user.userName;
    this.email = data.user.email;
    this.domain = data.user.domain;
    console.log('this.domain', this.domain);
    if (data.user.roles !== '') {
      this.roles = data.user.roles.split(',');
      this.oldRoles = this.roles;
    }
    this.password = data.user.password;
    this.confirmation = data.user.password;
    this.active = data.user.active;

    if (this.editMode) {
      this.userForm = fb.group({
        'userName': new FormControl({value: this.userName, disabled: true}, Validators.nullValidator),
        'email': [null, Validators.pattern],
        'roles': new FormControl(this.roles, Validators.required),
        'domain': this.multiDomain ? new FormControl(this.domain) : null,
        'password': [null, Validators.pattern],
        'confirmation': [null],
        'active': new FormControl({value: this.active, disabled: this.isCurrentUser()}, Validators.required)
      }, {
        validator: userValidatorService.validateForm
      });
    } else {
      this.formTitle = NEW_MODE;
      this.userForm = fb.group({
        'userName': new FormControl(this.userName, Validators.required),
        'email': [null, Validators.pattern],
        'roles': new FormControl(this.roles, Validators.required),
        'domain': this.multiDomain ? new FormControl(this.domain) : null,
        'password': [Validators.required, Validators.pattern],
        'confirmation': [Validators.required],
        'active': [Validators.required]
      }, {
        validator: userValidatorService.validateForm
      });
    }
  }

  updateUserName (event) {
    this.userName = event.target.value;
  }

  updateEmail (event) {
    this.email = event.target.value;
  }

  updatePassword (event) {
    this.password = event.target.value;
  }

  updateConfirmation (event) {
    this.confirmation = event.target.value;
  }

  updateActive (event) {
    this.active = event.target.checked;
  }

  submitForm () {
    this.dialogRef.close(true);
  }

  isCurrentUser (): boolean {
    return this.securityService.getCurrentUser().username === this.userName;
  }

  onRolesChanged () {
    // handle selection of roles
    if (!this.oldRoles.includes(ROLE_AP_ADMIN) && this.roles.includes(ROLE_AP_ADMIN)) { // the super admin role was selected->unselect all others
      this.roles = [ROLE_AP_ADMIN];
    } else if (this.oldRoles.includes(ROLE_AP_ADMIN) && this.roles.includes(ROLE_AP_ADMIN)) { // the super admin role is selected and user selects another role-> remove super-admin
      this.roles = this.roles.filter(el => el !== ROLE_AP_ADMIN);
    }
    this.oldRoles = this.roles;

    // it is here to avoid angular circular change detection error
    this.cdr.detectChanges();

    // handle domain
    this.isDomainDisabled = this.roles.includes(ROLE_AP_ADMIN);
    if (this.isDomainDisabled) {
      this.domain = null;
    }
  }
}
