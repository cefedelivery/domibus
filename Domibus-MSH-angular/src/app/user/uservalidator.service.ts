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

  validateUsers(modifiedUsers:UserResponseRO[], users:UserResponseRO[]):boolean{
    let errorMessage: string = "";
    if(modifiedUsers.length==0){
      return false;
    }
    errorMessage = errorMessage.concat(this.checkUserNameDuplication(users));
    for (let u in modifiedUsers) {
      let user: UserResponseRO = modifiedUsers[u];
      let number = users.indexOf(user) + 1;
      if(user.status===UserState[UserState.NEW]){
        errorMessage = errorMessage.concat(this.validateNewUsers(user,number));
      }
      else if(user.status===UserState[UserState.UPDATED]){
        errorMessage =errorMessage.concat(this.validateRoles(user,number));
        errorMessage =errorMessage.concat(this.validateEmail(user,number));
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
      errorMessage =errorMessage.concat(this.validateEmail(user,number));
      if (user.password == null || user.password.trim() === "") {
        errorMessage = errorMessage.concat("User " + number + " has no password defined\n");
      }

      return errorMessage;
  }

  checkUserNameDuplication(allUsers:UserResponseRO[]){
    let errorMessage:string="";
    let seen = new Set();
    var hasDuplicates=allUsers.every(function(user) {
      if(seen.size === seen.add(user.userName).size){
        errorMessage=errorMessage.concat("Duplicate user name with user "+allUsers.indexOf(user)+" ");
        return false;
      };
      return true;
    });
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

   validateEmail(user:UserResponseRO,number): string{
    let email:string=user.email;
    var EMAIL_REGEXP = /^[a-z0-9!#$%&'*+\/=?^_`{|}~.-]+@[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)*$/i;

    if (email!= "" && (email.length <= 5 || !EMAIL_REGEXP.test(email))) {
      return "incorrectMailFormat for user "+number;
    }
    return "";
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
