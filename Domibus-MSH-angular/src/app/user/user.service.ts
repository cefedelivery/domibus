import {UserResponseRO} from "./user";
import {Injectable} from "@angular/core";
import {Http,Response} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {Observable} from "rxjs/Observable";
@Injectable()
export class UserService {
  users:Array<UserResponseRO>;
  initialUsers:Array<UserResponseRO>;


  constructor(private http: Http,private alertService: AlertService) {

    /*this.initialUsers= [new User('thom', 'dussartt@gmail.com', '', true,UserState.PERSISTED),
      new User('thom 2', 'dussartt@gmail.com', '' ,true,UserState.PERSISTED),
      new User('thom 3', 'dussartt@gmail.com', '',true,UserState.PERSISTED)]
    this.users=[new User('thom', 'dussartt@gmail.com', '', true,UserState.PERSISTED),
      new User('thom 2', 'dussartt@gmail.com', '' ,true,UserState.PERSISTED),
      new User('thom 3', 'dussartt@gmail.com', '',true,UserState.PERSISTED)]*/
  }

  /*var USERS: Array<User> = [
  new User('thom', 'dussartt@gmail.com', '*****', 'OK'),
  new User('thom 2', 'dussartt@gmail.com', '*****', 'OK'),
  new User('thom 3', 'dussartt@gmail.com', '*****', 'OK')
]*/
  getUsers():Observable<UserResponseRO[]>{
    return this.http.get("rest/user/users")
      .map(this.extractData)
      .catch(this.handleError);
  }
    //return this.http.get("rest/user/users").toPromise().then(response=>response.json()._embedded.userResponseROs as  Array<UserResponseRO>).catch(); /*{
   /* this.http.get("rest/user/users").subscribe(
      (response: Response) => {
        debugger;
        let users = response.json().users;
        for (let key in users) {
          this.users.push(users[key])
        }
      },
      (error: Response) => {
        this.alertService.error('Could not load queues: ' + error);
      }
    )
    return Promise.resolve( this.users);*/
  //}

  saveUsers(users:Array<UserResponseRO>):void{

  }



private extractData(res: Response) {
  let body = res.json();
  return body || { };
}

private handleError (error: Response | any) {
  // In a real world app, we might use a remote logging infrastructure
  let errMsg: string;
  if (error instanceof Response) {
    const body = error.json() || '';
    const err = body.error || JSON.stringify(body);
    errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
  } else {
    errMsg = error.message ? error.message : error.toString();
  }
  console.error(errMsg);
  return Promise.reject(errMsg);
}


}
