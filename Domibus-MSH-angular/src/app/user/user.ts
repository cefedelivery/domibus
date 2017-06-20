export enum UserState{
  PERSISTED,
  NEW
}
export class UserResponseRO {
  userName: string;
  email: string;
  password:string;
  userState:UserState;
  active:boolean;
  authorities: Array<string>;
  roles:string="";



  constructor(userName: string, email: string, password: string, active: boolean, userState:UserState,authorities: Array<string>) {
    this.userName = userName;
    this.email = email;
    this.password = password;
    this.userState= userState;
    this.active = active;
    this.authorities=authorities;
    for(let authority in authorities){
      this.roles=this.roles.concat(authorities[authority]).concat(" ");
    }
    //debugger;
  }

  isNew():boolean{
    return this.userState==UserState.NEW;
  }
  getFirstAuthority():any{
    debugger;
    return this.authorities[0];
  }
}
