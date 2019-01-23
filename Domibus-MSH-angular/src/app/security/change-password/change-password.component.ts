import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {UserValidatorService} from '../../user/uservalidator.service';
import {SecurityService} from '../../security/security.service';
import {Http} from '@angular/http';
import {AlertService} from '../../common/alert/alert.service';
import {Router} from '@angular/router';

@Component({
  templateUrl: './change-password.component.html',
  providers: [UserValidatorService]
})

export class ChangePasswordComponent implements OnInit {

  currentPassword: string;
  password: string;
  confirmation: string;
  public passwordPattern: string;
  public passwordValidationMessage: string;
  userForm: FormGroup;

  constructor(private fb: FormBuilder, private securityService: SecurityService,
              private userValidatorService: UserValidatorService, private http: Http,
              private alertService: AlertService, private router: Router) {

    this.currentPassword = this.securityService.password;
    this.userForm = fb.group({
      'currentPassword':  [null],
      'password': [null],
      'confirmation': [null]
    }, {
      validator: userValidatorService.matchPassword
    });

    this.securityService.password = null;
  }

  async ngOnInit() {
    const passwordPolicy = await this.securityService.getPasswordPolicy();
    this.passwordPattern = passwordPolicy.pattern;
    this.passwordValidationMessage = passwordPolicy.validationMessage;
  }

  async submitForm() {
    const params = {
      currentPassword: this.currentPassword,
      newPassword: this.password
    };

    try {
      await this.securityService.changePassword(params);
      this.alertService.success('Password successfully changed.');
      this.router.navigate(['/']);
    } catch (error) {
      this.alertService.exception('Password could not be changed.', error);
    }
  }

}
