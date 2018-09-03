import {Component, OnInit} from "@angular/core";
import {MdDialog} from "@angular/material";
import {NavigationStart, Router} from "@angular/router";
import {isNullOrUndefined} from "util";

@Component({
  selector: 'page-helper',
  templateUrl: './page-helper.component.html',
  styleUrls: ['./page-helper.component.css']
})
export class PageHelperComponent implements OnInit {

  pageName: string;
  helpPages: Map<String, String> = new Map<String, String>();
  activateHelp: boolean = false;

  constructor(public dialog: MdDialog, private router: Router) {
  }

  ngOnInit() {
    let MAIN_HELP_PAGE = "https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus+v4.0+Admin+Console+Help";
    let VERSION_SPECIFIC_PAGE = "#Domibusv4.0AdminConsoleHelp-";

    this.helpPages.set("/", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "Messages");
    this.helpPages.set("/login", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "Login");
    this.helpPages.set("/messagefilter", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "MessageFilter");
    this.helpPages.set("/truststore", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "Truststore");
    this.helpPages.set("/pmode-current", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "PMode-Current");
    this.helpPages.set("/pmode-archive", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "PMode-Archive");
    this.helpPages.set("/pmode-party", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "PMode-Parties");
    this.helpPages.set("/errorlog", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "ErrorLog");
    this.helpPages.set("/jms", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "JMSMonitoring");
    this.helpPages.set("/user", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "Users");
    this.helpPages.set("/pluginuser", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "PluginUsers");
    this.helpPages.set("/audit", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "Audit");
    this.helpPages.set("/alerts", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "Alerts");
    this.helpPages.set("/testservice", MAIN_HELP_PAGE + VERSION_SPECIFIC_PAGE + "TestService");
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        console.log("Navigation change [" + event.url + "]");
        let page = this.helpPages.get(event.url);
        if (isNullOrUndefined(page)) {
          this.activateHelp = false;
        } else {
          this.activateHelp = true;
          this.pageName = page.toString();
        }
      }
    });
  }


  openHelpDialog() {
    window.open(this.pageName, "_blank");
  }

}
