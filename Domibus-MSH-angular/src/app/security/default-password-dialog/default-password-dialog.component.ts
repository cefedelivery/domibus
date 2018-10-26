import {Component, Inject, OnInit} from '@angular/core';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';

@Component({
  selector: 'app-default-password-dialog',
  templateUrl: './default-password-dialog.component.html',
  styleUrls: ['./default-password-dialog.component.css']
})
export class DefaultPasswordDialogComponent implements OnInit {

  reason: string;

  constructor (public dialogRef: MdDialogRef<DefaultPasswordDialogComponent>, @Inject(MD_DIALOG_DATA) public data: any) {
    this.reason = data;
  }

  ngOnInit () {
  }

}
