import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-rollback-dialog',
  templateUrl: './rollback-dialog.component.html',
  styleUrls: ['./rollback-dialog.component.css']
})
export class RollbackDialogComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<RollbackDialogComponent>) {
  }

  ngOnInit() {
  }

}
