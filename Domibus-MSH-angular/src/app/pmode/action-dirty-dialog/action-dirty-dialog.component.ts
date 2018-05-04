import {Component, Inject, Input, OnInit} from '@angular/core';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';

@Component({
  selector: 'app-action-dirty-dialog',
  templateUrl: './action-dirty-dialog.component.html',
  styleUrls: ['../pmode.component.css']
})
export class ActionDirtyDialogComponent implements OnInit {

  constructor(@Inject(MD_DIALOG_DATA) private data: { actionTitle: string, actionName: string, actionIconName: string },
              public dialogRef: MdDialogRef<ActionDirtyDialogComponent>) {
  }

  ngOnInit() {
  }

}
