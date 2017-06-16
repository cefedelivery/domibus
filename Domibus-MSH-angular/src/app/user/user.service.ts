import { Injectable } from '@angular/core';
import {USERS} from "./mock-users";
import {User} from "./user";
@Injectable()
export class UserService {
  getUsers():Promise<User[]>{
    return Promise.resolve( USERS);
  }
}
