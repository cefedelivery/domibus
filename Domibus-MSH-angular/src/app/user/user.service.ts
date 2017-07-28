import {UserResponseRO} from "./user";
import {Injectable} from "@angular/core";
import {Http,Response} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {Observable} from "rxjs/Observable";
@Injectable()
export class UserService {

  constructor(private http: Http,private alertService: AlertService) {

  }

  getUsers():Observable<UserResponseRO[]>{
    return this.http.get("rest/user/users")
      .map(this.extractData)
      .catch(this.handleError);
  }

  getUserRoles():Observable<String[]> {
    return this.http.get("rest/user/userroles")
      .map(this.extractData)
      .catch(this.handleError);
  }

  deleteUsers(users:Array<UserResponseRO>):void {
    this.http.post("rest/user/delete", users).subscribe(res => {
      this.alertService.success("User(s) deleted", false);
    }, err => {
      this.alertService.error(err, false);
    });
  }

  saveUsers(users:Array<UserResponseRO>):void{
    this.http.post("rest/user/save", users).subscribe(res => {
      this.changeUserStatus(users);
      this.alertService.success("User saved", false);
    }, err => {
      this.alertService.error(err, false);
    });
  }

  changeUserStatus(users:Array<UserResponseRO>){
    for(let u in users){
      users[u].status="PERSISTED";
      users[u].password="";
    }
  }


private extractData(res: Response) {
  let body = res.json();
  return body || { };
}

private handleError (error: Response | any) {
  this.alertService.error(error, false);
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
