import {Component, OnInit} from '@angular/core';
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-rollback-dirty-dialog',
  templateUrl: './rollback-dirty-dialog.component.html',
  styleUrls: ['./rollback-dirty-dialog.component.css']
})
export class RollbackDirtyDialogComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<RollbackDirtyDialogComponent>) {
  }

  ngOnInit() {
  }

}
