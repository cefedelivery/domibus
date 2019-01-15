import {Component, OnDestroy, OnInit} from "@angular/core";
import {Router} from "@angular/router";


@Component({
  moduleId: module.id,
  templateUrl: 'logout.component.html',
  styleUrls: ['./logout.component.css']
})


export class LogoutAuthExtProviderComponent implements OnInit, OnDestroy {

  constructor(private router: Router) {
  }

  ngOnDestroy(): void {
  }

  ngOnInit(): void {
  }

  login_again(): void {
    console.log('lets login again');
    this.router.navigateByUrl('/index.html');
  }

}
