import {Component} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {MdDialog} from "@angular/material";
import {PmodeUploadComponent} from "app/pmode-upload/pmode-upload.component";

@Component({
  moduleId: module.id,
  templateUrl: 'pmode.component.html',
  providers: [],
  styleUrls: ['./pmode.component.css']
})

export class PModeComponent {
  selectedOption: string;

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

}
