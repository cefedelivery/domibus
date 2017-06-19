export enum UserState{
  PERSISTED,
  NEW
}
export class User {
  userName: string;
  email: string;
  password:string;
  userState:UserState;
  active:boolean;


  constructor(userName: string, email: string, password: string, active: boolean, userState:UserState) {
    this.userName = userName;
    this.email = email;
    this.password = password;
    this.userState= userState;
    this.active = active;
  }

  isNew():boolean{
    return this.userState==UserState.NEW;
  }
}
