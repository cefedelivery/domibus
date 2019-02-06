import {Component} from "@angular/core";

@Component({
  moduleId: module.id,
  templateUrl: 'logout.component.html'
})

export class LogoutAuthExtProviderComponent {

  constructor() {
  }

  login_again(): void {
    //just redirect to context path
    let context = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));
    let url = window.location.protocol + "//" + window.location.host + context;
    window.location.href = url;
  }

}
