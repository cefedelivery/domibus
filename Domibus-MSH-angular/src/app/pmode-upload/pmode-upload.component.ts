import { Component, OnInit } from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-pmode-upload',
  templateUrl: './pmode-upload.component.html',
  styleUrls: ['./pmode-upload.component.css']
})
export class PmodeUploadComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<PmodeUploadComponent>) {}

  ngOnInit() {
  }

}
