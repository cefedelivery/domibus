import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MdDialogRef, MD_DIALOG_DATA} from '@angular/material';

@Component({
  selector: 'app-pmode-upload',
  templateUrl: './pmode-view.component.html',
  styleUrls: ['../../pmode.component.css']
})
export class PmodeViewComponent implements OnInit {

  public pMode: { metadata: any, content: string };
  pModeType: string;

  constructor (@Inject(MD_DIALOG_DATA) public data: { metadata: any, content: string },
               public dialogRef: MdDialogRef<PmodeViewComponent>) {
  }

  ngOnInit () {
    this.pMode = this.data;
    this.pModeType = this.pMode.metadata.current ? 'Current': 'Archive';
    //console.log('view:', this.data)
  }

  ok () {
    this.dialogRef.close();
  };
}
