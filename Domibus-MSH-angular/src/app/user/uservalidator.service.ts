import {UserResponseRO} from "./user";
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

  validateNewUsers(modifyUsers:UserResponseRO[], users:UserResponseRO[]):boolean {
    for (let u in modifyUsers) {
      let user: UserResponseRO = modifyUsers[u];
      let errorMessage: string = "";
      let number = users.indexOf(user) + 1;
      if (user.userName == null || user.userName.trim() === "") {
        errorMessage = errorMessage.concat("User " + number + " has no username defined\n");
      }
      if (user.roles == null || user.roles.trim() === "") {
        errorMessage = errorMessage.concat("User " + number + " has no role defined\n");
      }
      if (user.password == null || user.password.trim() === "") {
        errorMessage = errorMessage.concat("User " + number + " has no password defined\n");
      }
      if (errorMessage.trim()) {
        this.alertService.clearAlert();
        this.alertService.error(errorMessage);
        return false;
      }
      return true;
    }
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
