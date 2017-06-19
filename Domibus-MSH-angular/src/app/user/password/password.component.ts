import { Component, OnInit } from '@angular/core';
import {User} from "../user";

@Component({
  selector: 'app-password',
  templateUrl: './password.component.html',
  styleUrls: ['./password.component.css']
})
export class PasswordComponent implements OnInit {

  editedUser:User;
  constructor() { }

  ngOnInit() {
  }

}
