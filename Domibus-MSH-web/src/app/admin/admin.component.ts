import {Component, OnInit} from '@angular/core';

import {SecurityService} from '../security/security.service';
import {User} from "../security/user";

@Component({
  moduleId: module.id,
  templateUrl: 'admin.component.html',
  providers: []
})

export class AdminComponent implements OnInit{
  currentUser: User;

  ngOnInit(): void {
    console.log("AdminComponent onInit")
  }


  constructor(private securityService: SecurityService) {
    this.currentUser = this.securityService.getCurrentUser();


  }
}
