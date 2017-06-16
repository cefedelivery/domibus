import {Component, OnInit} from "@angular/core";
import {User} from "./user";
import {UserService} from "./user.service";



@Component({
  moduleId: module.id,
  templateUrl: 'user.component.html',
  providers: [UserService],
  styleUrls: ['./user.component.css']
})



export class UserComponent implements OnInit {
  users: User[];

  constructor(private userService:UserService) {

  }

  ngOnInit(): void {
    this.getUsers();
  }

  getUsers(): void {
    this.userService.getUsers().then(users => this.users= users);
  }




}
