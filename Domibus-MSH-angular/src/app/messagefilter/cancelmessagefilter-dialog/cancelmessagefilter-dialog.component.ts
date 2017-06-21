import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-messagefilter-dialog',
  templateUrl: './cancelmessagefilter-dialog.component.html',
  styleUrls: ['./cancelmessagefilter-dialog.component.css']
})
export class CancelMessagefilterDialogComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<CancelMessagefilterDialogComponent>) {
  }

  ngOnInit() {
  }
}

