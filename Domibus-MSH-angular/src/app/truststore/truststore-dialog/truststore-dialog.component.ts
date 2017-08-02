import {Component, OnInit} from "@angular/core";
import {TrustStoreEntry} from "../trustore.model";
import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-truststore-dialog',
  templateUrl: './truststore-dialog.component.html',
  styleUrls: ['./truststore-dialog.component.css']
})
export class TruststoreDialogComponent implements OnInit {

  trustStoreEntry: TrustStoreEntry;
  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  constructor(public dialogRef: MdDialogRef<TruststoreDialogComponent>) {
  }

  ngOnInit() {
  }

}
