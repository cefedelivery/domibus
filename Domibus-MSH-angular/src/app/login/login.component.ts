import {Component, OnInit} from "@angular/core";
import {Router, ActivatedRoute} from "@angular/router";
import {SecurityService} from "../security/security.service";
import {HttpEventService} from "../http/http.event.service";
import {AlertService} from "../alert/alert.service";
import {SecurityEventService} from "../security/security.event.service";

@Component({
  moduleId: module.id,
  templateUrl: 'login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit {
  model: any = {};
  loading = false;
  returnUrl: string;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private securityService: SecurityService,
              private httpEventService: HttpEventService,
              private alertService: AlertService,
              private securityEventService: SecurityEventService) {
  }

  ngOnInit() {
    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    this.httpEventService.subscribe((error) => {
      console.log("Received forbidden request event")
      this.securityService.logout();
    });

    this.securityEventService.onLoginSuccessEvent().subscribe(
      data => {
        console.log("Authentication successfull");
        this.router.navigate([this.returnUrl]);
      });

    this.securityEventService.onLoginErrorEvent().subscribe(
      error => {
        console.error("Error authenticating:" + error);
        let message;
        const HTTP_UNAUTHORIZED = 401;
        const HTTP_FORBIDDEN = 403;
        const HTTP_GATEWAY_TIMEOUT = 504;
        switch (error.status) {
          case HTTP_UNAUTHORIZED:
          case HTTP_FORBIDDEN:
            message = "The username/password combination you provided are not valid. Please try again or contact your administrator.";
            break;
          case HTTP_GATEWAY_TIMEOUT:
            message = "Unable to login. Domibus is not running.";
            break;
          default:
            message = "Default error (" + error.status + ") occurred during login.";
            break;
        }
        this.alertService.error(message);
      });
  }

  login() {
    this.securityService.login(this.model.username, this.model.password);
  }
}
