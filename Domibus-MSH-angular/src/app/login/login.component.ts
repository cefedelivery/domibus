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
        this.alertService.error("Invalid username or password");
      });
  }

  login() {
    this.securityService.login(this.model.username, this.model.password);
  }
}
