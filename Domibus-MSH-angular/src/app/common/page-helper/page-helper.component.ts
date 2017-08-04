import {Component, OnInit} from "@angular/core";
import {MdDialog, MdDialogRef} from "@angular/material";
import {HelpDialogComponent} from "./help-dialog/help-dialog.component";
import {NavigationStart, Router} from "@angular/router";
import {Observable} from "rxjs/Observable";
import {Message} from "@angular/compiler/src/i18n/i18n_ast";

@Component({
  selector: 'page-helper',
  templateUrl: './page-helper.component.html',
  styleUrls: ['./page-helper.component.css']
})
export class PageHelperComponent implements OnInit {

  pageName: String;
  message: Observable<Message>;
  helpPages: Map<String, String> = new Map<String, String>();
  activateHelp: boolean = false;

  constructor(public dialog: MdDialog, private router: Router) {
    this.pageName = 'truststore';
  }

  ngOnInit() {
    this.helpPages.set("/", "messagelog");
    this.helpPages.set("/messagefilter", "messagefilter");
    this.helpPages.set("/truststore", "truststore");
    this.helpPages.set("/pmode", "pmode");
    this.helpPages.set("/errorlog", "errorlog");
    this.helpPages.set("/jms", "jms");
    this.helpPages.set("/user", "user");
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        console.log("Navigation change [" + event.url + "]");
        var pageName: String = this.helpPages.get(event.url);
        if (!pageName) {
          this.activateHelp = false;
        }
        else {
          this.activateHelp = true;
          this.pageName = pageName;
        }
      }
    });
  }


  openHelpDialog() {

    let dialogRef: MdDialogRef<HelpDialogComponent> = this.dialog.open(HelpDialogComponent, {data: {pageName: this.pageName}});
    dialogRef.afterClosed().subscribe(result => {

    });
  }

}
