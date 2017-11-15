import {Component, OnDestroy, OnInit} from "@angular/core";
import {Router, ActivatedRoute} from "@angular/router";
import {SecurityService} from "../security/security.service";
import {HttpEventService} from "../http/http.event.service";
import {AlertService} from "../alert/alert.service";
import {SecurityEventService} from "../security/security.event.service";
import {User} from "../security/user";
import {MdDialogRef, MdDialog} from "@angular/material";
import {DefaultPasswordDialogComponent} from "app/security/default-password-dialog/default-password-dialog.component";

@Component({
  moduleId: module.id,
  templateUrl: 'login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit, OnDestroy {


  model: any = {};
  loading = false;
  returnUrl: string;
  sub: any;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private securityService: SecurityService,
              private httpEventService: HttpEventService,
              private alertService: AlertService,
              private securityEventService: SecurityEventService,
              private dialog: MdDialog) {
  }

  ngOnInit() {
    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    this.httpEventService.subscribe((error) => {
      console.log("Received forbidden request event")
      this.securityService.logout();
    });

    this.sub = this.securityEventService.onLoginSuccessEvent().subscribe(
      data => {
        console.log("Authentication successfull");
        this.verifyDefaultLoginUsed();
        this.router.navigate([this.returnUrl]);
      });

    this.securityEventService.onLoginErrorEvent().subscribe(
      error => {
        console.error("Error authenticating:" + error);
        let message;
        const HTTP_UNAUTHORIZED = 401;
        const HTTP_FORBIDDEN = 403;
        const HTTP_NOTFOUND = 404;
        const HTTP_GATEWAY_TIMEOUT = 504;
        const USER_INACTIVE = 'Inactive';
        const USER_SUSPENDED = 'Suspended';
        switch (error.status) {
          case HTTP_UNAUTHORIZED:
          case HTTP_FORBIDDEN:
            let forbiddenCode = error.json().message;
            console.log("User forbiden code " + forbiddenCode);
            switch (forbiddenCode) {
              case USER_INACTIVE:
                message = "The user is inactive. Please contact your administrator.";
                break;
              case USER_SUSPENDED:
                message = "The user is suspended. Please try again later or contact your administrator.";
                break;
              default:
                message = "The username/password combination you provided are not valid. Please try again or contact your administrator.";
                break;
            }
            break;
          case HTTP_GATEWAY_TIMEOUT:
          case HTTP_NOTFOUND:
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

  verifyDefaultLoginUsed() {
    let currentUser: User = this.securityService.getCurrentUser();
    if (currentUser.defaultPasswordUsed) {
      this.dialog.open(DefaultPasswordDialogComponent);
    }
  }

  ngOnDestroy(): void {
    console.log("Destroying login component");
    this.sub.unsubscribe();
  }
}
