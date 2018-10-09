import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router, ActivatedRoute, NavigationStart} from '@angular/router';
import {SecurityService} from '../security/security.service';
import {AlertService} from '../alert/alert.service';
import {SecurityEventService} from '../security/security.event.service';
import {MdDialog} from '@angular/material';
import {DefaultPasswordDialogComponent} from 'app/security/default-password-dialog/default-password-dialog.component';

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

  constructor (private route: ActivatedRoute,
               private router: Router,
               private securityService: SecurityService,
               private alertService: AlertService,
               private securityEventService: SecurityEventService,
               private dialog: MdDialog) {

  }

  ngOnInit () {
    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    this.sub = this.securityEventService.onLoginSuccessEvent()
      .subscribe(
        () => this.onLoginSuccessEvent()
      );

    this.securityEventService.onLoginErrorEvent().subscribe(
      error => {
        console.error('Error authenticating:' + error);
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
            const forbiddenCode = error.json().message;
            console.log('User forbidden code ' + forbiddenCode);
            switch (forbiddenCode) {
              case USER_INACTIVE:
                message = 'The user is inactive. Please contact your administrator.';
                break;
              case USER_SUSPENDED:
                message = 'The user is suspended. Please try again later or contact your administrator.';
                break;
              default:
                message = 'The username/password combination you provided are not valid. Please try again or contact your administrator.';
                break;
            }
            break;
          case HTTP_GATEWAY_TIMEOUT:
          case HTTP_NOTFOUND:
            message = 'Unable to login. Domibus is not running.';
            break;
          default:
            message = 'Default error (' + error.status + ') occurred during login.';
            break;
        }
        this.alertService.error(message);
      });
  }

  login () {
    this.securityService.login(this.model.username, this.model.password);
  }

  onLoginSuccessEvent () {
    const changePassword = this.securityService.shouldChangePassword();
    if (changePassword.response) {
      this.dialog.open(DefaultPasswordDialogComponent, {data: changePassword.reason});
      this.router.navigate(['/user']);
    } else {
      this.router.navigate([this.returnUrl]);
    }
  }

  // async onLoginSuccessEvent () {
  //   const mustChangePassword = await this.securityService.mustChangePassword();
  //   if (mustChangePassword) {
  //     this.dialog.open(DefaultPasswordDialogComponent);
  //     this.router.navigate(['/user']);
  //   } else {
  //     this.router.navigate([this.returnUrl]);
  //   }
  // }

  ngOnDestroy (): void {
    console.log('Destroying login component');
    this.sub.unsubscribe();
  }
}
