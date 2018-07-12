import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-restore-dialog',
  templateUrl: './restore-dialog.component.html',
  styleUrls: ['../pmode.component.css']
})
export class RestoreDialogComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<RestoreDialogComponent>) {
  }

  ngOnInit() {
  }

}
