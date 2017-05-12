import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-messagelog-dialog',
  templateUrl: './messagelog-dialog.component.html',
  styleUrls: ['./messagelog-dialog.component.css']
})
export class MessagelogDialogComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<MessagelogDialogComponent>) {
  }

  ngOnInit() {
  }

}
