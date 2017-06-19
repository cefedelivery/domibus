import {User, UserState} from "./user";
import {Injectable} from "@angular/core";
@Injectable()
export class UserService {
  users:Array<User>;
  initialUsers:Array<User>;


  constructor() {
    this.initialUsers=[new User('thom', 'dussartt@gmail.com', '*****', true,UserState.PERSISTED),
      new User('thom 2', 'dussartt@gmail.com', '*****' ,true,UserState.PERSISTED),
      new User('thom 3', 'dussartt@gmail.com', '*****',true,UserState.PERSISTED)]
    this.users=[new User('thom', 'dussartt@gmail.com', '*****', true,UserState.PERSISTED),
      new User('thom 2', 'dussartt@gmail.com', '*****' ,true,UserState.PERSISTED),
      new User('thom 3', 'dussartt@gmail.com', '*****',true,UserState.PERSISTED)]
  }

  /*var USERS: Array<User> = [
  new User('thom', 'dussartt@gmail.com', '*****', 'OK'),
  new User('thom 2', 'dussartt@gmail.com', '*****', 'OK'),
  new User('thom 3', 'dussartt@gmail.com', '*****', 'OK')
]*/
  getUsers():Promise<Array<User>>{
    return Promise.resolve( this.users);
  }

  saveUsers(users:Array<User>):void{

  }




}
