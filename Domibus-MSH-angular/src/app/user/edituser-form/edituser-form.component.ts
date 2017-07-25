import {Component, Inject, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MD_DIALOG_DATA, MdDialogRef} from "@angular/material";
import {UserValidatorService} from "../uservalidator.service";

@Component({
  selector: 'edituser-form',
  templateUrl: 'edituser-form.component.html',
  providers:[UserValidatorService]
})

export class EditUserComponent implements OnInit {

  existingRoles = [];

  username = '';
  email = '';
  active = true;
  roles = [];

  userForm: FormGroup;

  constructor(public dialogRef: MdDialogRef<EditUserComponent>,
              @Inject(MD_DIALOG_DATA) public data: any,
              fb: FormBuilder,
              userValidatorService:UserValidatorService) {

    this.username = data.userName;
    this.email = data.email;
    this.roles = data.roles.split(",");

    this.userForm = fb.group({
      'username': [Validators.required],
      'email': [null],
      'roles': [Validators.required],
      'password': [null, Validators.compose([Validators.required, Validators.minLength(8), Validators.maxLength(32)])],
      'confirmation': [null, Validators.required],
      'active': [Validators.required]
    });
  }

  ngOnInit() {
    this.existingRoles = ["USER_ADMIN", "USER_ROLE"];
  }

  updateUsername(event) {

  }

  selectRoles(event) {

  }

  submitForm() {

  }


}
