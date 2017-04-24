import {Component, OnInit, ViewChild} from '@angular/core';
import {MdDialogRef} from "@angular/material";
import {AlertService} from "../alert/alert.service";
import {Http} from "@angular/http";

@Component({
  selector: 'app-pmode-upload',
  templateUrl: './pmode-upload.component.html',
  styleUrls: ['./pmode-upload.component.css']
})
export class PmodeUploadComponent implements OnInit {

  private url = "/rest/pmode";

  constructor(public dialogRef: MdDialogRef<PmodeUploadComponent>, private http: Http, private alertService: AlertService) {
  }

  ngOnInit() {
  }

  @ViewChild('fileInput')
  private fileInput;

  public submit() {
    let fi = this.fileInput.nativeElement;
    let input = new FormData();
    input.append('file', fi.files[0]);
    this.http.post(this.url, input).subscribe(res => {
        this.alertService.success(res.json(), false);
      }, err => {
        this.alertService.error(err.json(), false);
      }
    );
    this.dialogRef.close();
  }

}
