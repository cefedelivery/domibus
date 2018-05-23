import {Component, OnInit, ViewChild} from "@angular/core";
import {SecurityService} from "./security/security.service";
import {Router, RouterOutlet} from "@angular/router";
import {SecurityEventService} from "./security/security.event.service";
import {Title} from "@angular/platform-browser";
import {Http, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  isAdmin: boolean;
  _currentUser: string;
  fullMenu: boolean = true;
  menuClass: string = this.fullMenu ? "menu-expanded" : "menu-collapsed";

  @ViewChild(RouterOutlet)
  outlet: RouterOutlet;

  constructor(private securityService: SecurityService,
              private router: Router,
              private securityEventService: SecurityEventService,
              private http: Http,
              private titleService: Title) {
    let applicationNameResponse: Observable<Response> = this.http.get('rest/application/name');

    applicationNameResponse.subscribe((name: Response) => {
      this.titleService.setTitle(name.json());
    });
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

  get currentUser(): string {
    let user = this.securityService.getCurrentUser();
    return user ? user.username : "";
  }

  logout(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/login']).then((ok) => {
      if (ok) {
        this.securityService.logout();
      }
    })
  }

  toggleMenu() {
    this.fullMenu = !this.fullMenu
    this.menuClass = this.fullMenu ? "menu-expanded" : "menu-collapsed"
    setTimeout(() => {
      var evt = document.createEvent("HTMLEvents")
      evt.initEvent('resize', true, false)
      window.dispatchEvent(evt)
    }, 500)
    //ugly hack but otherwise the ng-datatable doesn't resize when collapsing the menu
    //alternatively this can be tried (https://github.com/swimlane/ngx-datatable/issues/193) but one has to implement it on every page
    //containing a ng-datatable and it only works after one clicks inside the table
  }

}
