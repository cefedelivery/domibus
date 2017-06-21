import {Component} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {MdDialog} from "@angular/material";
import {PmodeUploadComponent} from "app/pmode-upload/pmode-upload.component";

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import * as FileSaver from "file-saver";

@Component({
  moduleId: module.id,
  templateUrl: 'pmode.component.html',
  providers: [],
  styleUrls: ['./pmode.component.css']
})

export class PModeComponent {
  private selectedOption: string;
  private url = "/rest/pmode";

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  ngOnInit() {
  }

  upload() {
    let dialogRef = this.dialog.open(PmodeUploadComponent);
    dialogRef.afterClosed().subscribe(result => {
      this.selectedOption = result;
    });
  }

  download() {
    this.http.get(this.url).subscribe(res => {
      this.downloadFile(res);
    }, err => {
      this.alertService.error(err.json());
    });

  }

  private downloadFile(data: any) {
    var blob = new Blob([data._body], {type: 'text/xml'});
    FileSaver.saveAs(blob, "Pmodes.xml");
  }
}

