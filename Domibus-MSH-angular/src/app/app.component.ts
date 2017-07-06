import {Component, OnInit} from "@angular/core";
import {SecurityService} from "./security/security.service";
import {Router} from "@angular/router";
import {SecurityEventService} from "./security/security.event.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  isAdmin: boolean;

  constructor(private securityService: SecurityService,
              private router: Router,
              private securityEventService: SecurityEventService) {
  }

  ngOnInit() {
    this.securityEventService.onLoginSuccessEvent().subscribe(
      data => {
        this.isAdmin = this.securityService.isCurrentUserAdmin();
      });

    this.securityEventService.onLoginErrorEvent().subscribe(
      error => {
        this.isAdmin = this.securityService.isCurrentUserAdmin();
      });

    this.securityEventService.onLogoutSuccessEvent().subscribe(
      data => {
        this.isAdmin = this.securityService.isCurrentUserAdmin();
        this.router.navigate(['/login']);
      });
  }

  hasAdmin(): boolean {
    return this.securityService.isCurrentUserAdmin();
  }

  logout(event: Event): void {
    event.preventDefault();
    this.securityService.logout();
  }
}
