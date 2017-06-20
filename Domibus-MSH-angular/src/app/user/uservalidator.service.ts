import {UserResponseRO} from "./user";
import {AlertService} from "../alert/alert.service";
import {Injectable} from "@angular/core";
/**
 * Created by dussath on 6/20/17.
 */
@Injectable()
export class UserValidatorService{

  constructor(private alertService: AlertService) {
  }

  validateUsers(modifyUsers:UserResponseRO[],users:UserResponseRO[]):boolean{
    for(let u in modifyUsers){
      let user:UserResponseRO=modifyUsers[u];
      let errorMessage:string="";
      let number = users.indexOf(user)+1;
      if(user.userName==null || user.userName.trim()===""){
        errorMessage=errorMessage.concat( "User "+ number+" has no username defined\n");
      }
      if(user.roles==null || user.roles.trim()===""){
        errorMessage=errorMessage.concat("User "+ number +" has no role defined\n");
      }
      debugger;
      if(user.password==null || user.password.trim()===""){
        errorMessage=errorMessage.concat("User "+number+" has no password defined\n");
      }
      if(errorMessage.trim()){
        this.alertService.clearAlert();
        this.alertService.error(errorMessage);
        return false;
      }
      return true;
    }
  }

}
