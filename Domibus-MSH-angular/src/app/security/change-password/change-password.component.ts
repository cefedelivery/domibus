import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {UserValidatorService} from '../../user/uservalidator.service';
import {SecurityService} from '../../security/security.service';

@Component({
  templateUrl: './change-password.component.html',
  providers: [UserValidatorService]
})

export class ChangePasswordComponent implements OnInit {

  confirmation: string;
  public passwordPattern: string;
  public passwordValidationMessage: string;
  userForm: FormGroup;

  constructor(fb: FormBuilder, private securityService: SecurityService, userValidatorService: UserValidatorService) {
    this.userForm = fb.group({
      'password': [Validators.required, Validators.pattern],
      'confirmation': [Validators.required],
    }, {
      validator: userValidatorService.matchPassword
    });
  }

  async ngOnInit() {
    const passwordPolicy = await this.securityService.getPasswordPolicy();
    this.passwordPattern = passwordPolicy.pattern;
    this.passwordValidationMessage = passwordPolicy.validationMessage;
  }

  submitForm() {

  }

}
