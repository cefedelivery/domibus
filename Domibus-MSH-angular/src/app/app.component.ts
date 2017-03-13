import {Component, OnInit} from "@angular/core";
import {SecurityService} from "./security/security.service";
import {User} from "./security/user";
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  isAdmin: boolean;
  isActive: boolean;

  constructor(private securityService: SecurityService, private router: Router) {
  }

  ngOnInit() {
    this.checkAdmin();

    this.router.events.subscribe((val) => {
      // see also
      console.log("---------checking admin");

      this.checkAdmin();
    });
  }

  checkAdmin() {
    this.isAdmin = false;
    let currentUser = this.securityService.getCurrentUser();
    if (currentUser && currentUser.authorities) {
      if (currentUser.authorities.indexOf('ROLE_ADMIN') !== -1) {
        this.isAdmin = true;
      }
    }
  }

  logout(event:Event):void {
    event.preventDefault();
    this.securityService.logout();
  }
}
