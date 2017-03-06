import {Component, OnInit} from '@angular/core';

import {SecurityService} from '../security/security.service';
import {User} from "../security/user";

@Component({
  moduleId: module.id,
  templateUrl: 'home.component.html',
  providers: [],
  styleUrls: ['./home.component.css']
})

export class HomeComponent implements OnInit{
  currentUser: User;

  ngOnInit(): void {
    console.log("HomeComponent onInit")
    this.currentUser = this.securityService.getCurrentUser();
  }

  constructor(private securityService: SecurityService) {
  }

  logout(event:Event):void {
    event.preventDefault();
    this.securityService.logout();
  }

  testPost(event:Event):void {
    event.preventDefault();
    console.log("calling test post")
    this.securityService.testPost("testUser", "testPassw");
  }

  testGet(event:Event):void {
    event.preventDefault();
    console.log("calling test get")
    this.securityService.testGet();
  }

}
