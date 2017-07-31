import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-messagefilter-dialog',
  templateUrl: './savedialog.component.html',
  styleUrls: ['./savedialog.component.css']
})
export class MessagefilterDialogComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<MessagefilterDialogComponent>) {
  }

  ngOnInit() {
  }
}

