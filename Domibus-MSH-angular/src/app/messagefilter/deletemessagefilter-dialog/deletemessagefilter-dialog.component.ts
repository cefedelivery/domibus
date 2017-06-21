import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-messagefilter-dialog',
  templateUrl: './deletemessagefilter-dialog.component.html',
  styleUrls: ['./deletemessagefilter-dialog.component.css']
})
export class DeleteMessagefilterDialogComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<DeleteMessagefilterDialogComponent>) {
  }

  ngOnInit() {
  }
}

