import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";
import {AppComponent} from "../../app.component";

@Component({
  selector: 'app-messagelog-details',
  templateUrl: './messagelog-details.component.html',
  styleUrls: ['./messagelog-details.component.css']
})
export class MessagelogDetailsComponent implements OnInit {

  message;
  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';
  fourCornerEnabled;

  constructor(public dialogRef: MdDialogRef<MessagelogDetailsComponent>) {
  }

  ngOnInit() {
  }

}
