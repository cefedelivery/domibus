import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-messagelog-details',
  templateUrl: './messagelog-details.component.html',
  styleUrls: ['./messagelog-details.component.css']
})
export class MessagelogDetailsComponent implements OnInit {

  message;
  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  constructor(public dialogRef: MdDialogRef<MessagelogDetailsComponent>) {
  }

  ngOnInit() {
  }

}
