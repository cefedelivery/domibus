import {Component, OnInit, ViewChild} from '@angular/core';
import {SecurityService} from './security/security.service';
import {NavigationStart, Router, RouterOutlet} from '@angular/router';
import {SecurityEventService} from './security/security.event.service';
import {Title} from '@angular/platform-browser';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  // isAdmin: boolean;
  fullMenu: boolean = true;
  menuClass: string = this.fullMenu ? 'menu-expanded' : 'menu-collapsed';
  fourCornerEnabled: boolean = true;

  @ViewChild(RouterOutlet)
  outlet: RouterOutlet;

  constructor (private securityService: SecurityService,
               private router: Router,
               private securityEventService: SecurityEventService,
               private http: Http,
               private titleService: Title) {
    const applicationNameResponse: Observable<Response> = this.http.get('rest/application/name');

    applicationNameResponse.subscribe((name: Response) => {
      this.titleService.setTitle(name.json());
    });

    const fourCornerModelResponse: Observable<Response> = this.http.get('rest/application/fourcornerenabled');

    fourCornerModelResponse.subscribe((name: Response) => {
      this.fourCornerEnabled = name.json();
    });
  }

  ngOnInit () {
    this.securityEventService.onLogoutSuccessEvent().subscribe(
      data => {
        this.router.navigate(['/login']);
      });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        if (event.url == '/login') {
          if (!!this.securityService.getCurrentUser()) {
            this.router.navigate(['/']);
          }

        }
      }
    });
  }

  isAdmin (): boolean {
    return this.securityService.isCurrentUserAdmin();
  }

  isUser (): boolean {
    return !!this.currentUser;
  }

  get currentUser (): string {
    const user = this.securityService.getCurrentUser();
    return user ? user.username : '';
  }

  logout (event: Event): void {
    event.preventDefault();
    this.router.navigate(['/login']).then((ok) => {
      if (ok) {
        this.securityService.logout();
      }
    })
  }

  toggleMenu () {
    this.fullMenu = !this.fullMenu
    this.menuClass = this.fullMenu ? 'menu-expanded' : 'menu-collapsed'
    setTimeout(() => {
      var evt = document.createEvent('HTMLEvents')
      evt.initEvent('resize', true, false)
      window.dispatchEvent(evt)
    }, 500)
    //ugly hack but otherwise the ng-datatable doesn't resize when collapsing the menu
    //alternatively this can be tried (https://github.com/swimlane/ngx-datatable/issues/193) but one has to implement it on every page
    //containing a ng-datatable and it only works after one clicks inside the table
  }

}
