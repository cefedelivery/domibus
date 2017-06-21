import {UserResponseRO, UserState} from "./user";
import {AlertService} from "../alert/alert.service";
import {Injectable} from "@angular/core";
import {AbstractControl} from "@angular/forms";
/**
 * Created by dussath on 6/20/17.
 */
@Injectable()
export class UserValidatorService{

  constructor(private alertService: AlertService) {
  }

  validateUsers(modifyUsers:UserResponseRO[], users:UserResponseRO[]):boolean{
    let errorMessage: string = "";
    for (let u in modifyUsers) {
      let user: UserResponseRO = modifyUsers[u];
      let number = users.indexOf(user) + 1;
      if(user.status===UserState[UserState.NEW]){
        errorMessage = errorMessage.concat(this.validateNewUsers(user,number));
      }
      else if(user.status===UserState[UserState.UPDATED]){
        errorMessage =errorMessage.concat(this.validateRoles(user,number));
      }
    }
    return this.triggerValidation(errorMessage);
  }


  validateNewUsers(user:UserResponseRO, number):string{
      let errorMessage: string = "";
      if (user.userName == null || user.userName.trim() === "") {
        errorMessage = errorMessage.concat("User " + number + " has no username defined\n");
      }
      errorMessage =errorMessage.concat(this.validateRoles(user,number));

      if (user.password == null || user.password.trim() === "") {
        errorMessage = errorMessage.concat("User " + number + " has no password defined\n");
      }

      return errorMessage;
  }

   triggerValidation(errorMessage:string):boolean{
     if (errorMessage.trim()) {
       this.alertService.clearAlert();
       this.alertService.error(errorMessage);
       return false;
     }
     return true;
   }

    validateRoles(user:UserResponseRO,number):string {
      let errorMessage: string = "";
      if (user.roles == null || user.roles.trim() === "") {
        errorMessage = errorMessage.concat("User " + number + " has no role defined\n");
      }
      else {
        let authorities = user.roles.split(",");
        for (let a in authorities) {
          if (authorities[a].trim().toUpperCase() !== "ROLE_USER" && authorities[a].trim().toUpperCase() !== "ROLE_ADMIN") {
            errorMessage = errorMessage.concat("Role should be ROLE_USER or ROLE_ADMIN separated by ,\n");
          }
        }
      }
      return errorMessage;
    }



  matchPassword(form: AbstractControl) {
      let password = form.get('password').value; // to get value in input tag
      let confirmPassword = form.get('passwordConfirmation').value; // to get value in input tag
      if(password != confirmPassword) {
        console.log('false');
        form.get('passwordConfirmation').setErrors( {confirmation: true} )
      } else {
        console.log('true');
        return null
      }
    }


}
