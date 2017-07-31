import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-messagefilter-dialog',
  templateUrl: './canceldialog.component.html',
  styleUrls: ['./canceldialog.component.css']
})
export class CancelDialogComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<CancelDialogComponent>) {
  }

  ngOnInit() {
  }
}

