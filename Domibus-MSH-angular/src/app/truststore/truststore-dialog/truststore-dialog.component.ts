import {Component, Inject} from "@angular/core";
import {TrustStoreEntry} from "../trustore.model";
import {MD_DIALOG_DATA, MdDialogRef} from "@angular/material";

/**
 * @Author Dussart Thomas
 * @Since 3.3
 */

@Component({
  selector: 'app-truststore-dialog',
  templateUrl: './truststore-dialog.component.html',
  styleUrls: ['./truststore-dialog.component.css']
})
export class TruststoreDialogComponent {

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';
  trustStoreEntry: TrustStoreEntry;

  constructor(public dialogRef: MdDialogRef<TruststoreDialogComponent>, @Inject(MD_DIALOG_DATA) public data: any) {
    this.trustStoreEntry = data.trustStoreEntry;
  }

}
