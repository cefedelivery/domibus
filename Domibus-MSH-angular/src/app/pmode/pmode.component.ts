import {Component} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {MdDialog} from "@angular/material";
import {PmodeUploadComponent} from "app/pmode-upload/pmode-upload.component";

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import * as FileSaver from "file-saver";
import {isNullOrUndefined} from "util";

@Component({
  moduleId: module.id,
  templateUrl: 'pmode.component.html',
  providers: [],
  styleUrls: ['./pmode.component.css']
})

export class PModeComponent {
  private ERROR_PMODE_EMPTY = "As PMode is empty, no file was downloaded.";
  private selectedOption: string;
  private url = "rest/pmode";

  private pModeExists = false;
  private pModeContents: string = '';

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  ngOnInit() {
    this.checkPmodeActive();
  }

  checkPmodeActive() {
    if(!isNullOrUndefined(this.url)) {
      this.http.get(this.url).subscribe(res => {

        const HTTP_OK = 200;
        if(res.status == HTTP_OK) {
          this.pModeExists = true;
          this.pModeContents = res.text();
        }
      }, err => {
        this.pModeExists = false;
      })
    }
  }

  upload() {
    let dialogRef = this.dialog.open(PmodeUploadComponent);
    dialogRef.afterClosed().subscribe(result => {
      this.selectedOption = result;
      this.checkPmodeActive();
    });
  }

  download() {
    if(this.pModeExists) {
      this.http.get(this.url).subscribe(res => {
        PModeComponent.downloadFile(res.text());
      }, err => {
        this.alertService.error(err._body);
      });
    } else {
      this.alertService.error(this.ERROR_PMODE_EMPTY)
    }

  }

  private static downloadFile(data: any) {
    const blob = new Blob([data], {type: 'text/xml'});
    FileSaver.saveAs(blob, "Pmodes.xml");
  }
}

