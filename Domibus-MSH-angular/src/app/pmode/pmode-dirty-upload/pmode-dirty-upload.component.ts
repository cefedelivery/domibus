import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-pmode-dirty-upload',
  templateUrl: './pmode-dirty-upload.component.html',
  styleUrls: ['./pmode-dirty-upload.component.css']
})
export class PmodeDirtyUploadComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<PmodeDirtyUploadComponent>) {
  }

  ngOnInit() {
  }

}
