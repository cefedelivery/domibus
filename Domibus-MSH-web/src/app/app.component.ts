import {Component} from "@angular/core";
import {SecurityService} from "./security/security.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  constructor(private securityService: SecurityService) {
  }

  logout(event:Event):void {
    event.preventDefault();
    this.securityService.logout();
  }
}
