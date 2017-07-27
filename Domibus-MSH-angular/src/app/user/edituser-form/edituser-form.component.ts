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

  password:any;
  confirmation:any;
  userName = '';
  email = '';
  active = true;
  roles = [];

  editMode: boolean;

  userForm: FormGroup;

  constructor(public dialogRef: MdDialogRef<EditUserComponent>,
              @Inject(MD_DIALOG_DATA) public data: any,
              fb: FormBuilder,
              userValidatorService:UserValidatorService) {

    this.editMode = data.edit;
    this.userName = data.user.userName;
    this.email = data.user.email;
    this.roles = data.user.roles.split(",");
    this.password = data.user.password;
    this.confirmation = data.user.password;
    this.active = data.user.active;

    if(this.editMode) {
      this.userForm = fb.group({
        'userName': [],
        'email': [null, Validators.pattern],
        'roles': [Validators.required],
        'password': [null, Validators.compose([Validators.minLength(8), Validators.maxLength(32)])],
        'confirmation': [null],
        'active': [Validators.required]
      }, {
        validator: userValidatorService.matchPassword
      });
    } else {
      this.userForm = fb.group({
        'userName': [Validators.required],
        'email': [null, Validators.pattern],
        'roles': [Validators.required],
        'password': [Validators.compose([Validators.required, Validators.minLength(8), Validators.maxLength(32)])],
        'confirmation': [Validators.required],
        'active': [Validators.required]
      }, {
        validator: userValidatorService.matchPassword
      });
    }
  }

  ngOnInit() {
    this.existingRoles = ["ROLE_ADMIN", "ROLE_USER"];
  }

  updateUserName(event) {
    this.userName = event.target.value;
  }

  updateEmail(event) {
    this.email = event.target.value;
  }

  updatePassword(event) {
    this.password = event.target.value;
  }

  updateConfirmation(event) {
    this.confirmation = event.target.value;
  }

  updateActive(event) {
    this.active = event.target.checked;
  }

  submitForm() {
    debugger;
    this.dialogRef.close(true);
  }


}
