import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";
import {isNullOrUndefined} from "util";

@Component({
  selector: 'app-move-dialog',
  templateUrl: './move-dialog.component.html',
  styleUrls: ['./move-dialog.component.css']
})
export class MoveDialogComponent implements OnInit {

  selectedSource: any;
  destinationsChoiceDisabled: boolean = false;
  queues: Array<any> = [];

  constructor(public dialogRef: MdDialogRef<MoveDialogComponent>) {
  }

  ngOnInit() {
  }

  canOk(): boolean {
    return !isNullOrUndefined(this.selectedSource);
  }

}
