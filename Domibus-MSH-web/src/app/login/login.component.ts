import {Component, OnInit} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';

import {SecurityService} from '../security/security.service';
import {HttpEventService} from "../http/http.event.service";
import {AlertService} from "../alert/alert.service";

@Component({
  moduleId: module.id,
  templateUrl: 'login.component.html'
})

export class LoginComponent implements OnInit {
  model: any = {};
  loading = false;
  returnUrl: string;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private securityService: SecurityService,
              private httpEventService: HttpEventService,
              private alertService: AlertService) {
  }

  ngOnInit() {
     // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    this.httpEventService.subscribe((error) => {
      console.log("Received forbidden request event")
      this.securityService.logout();
    });
  }

  login() {
    this.loading = true;
    this.securityService.login(this.model.username, this.model.password)
      .subscribe(
        data => {
          this.router.navigate([this.returnUrl]);
        },
        error => {
          console.error("Error authenticating:" + error);
          this.alertService.error("Invalid username or password");
          this.loading = false;
        });
  }
}
