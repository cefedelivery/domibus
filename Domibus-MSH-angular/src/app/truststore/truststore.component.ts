import {Component, OnInit} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
import "rxjs/add/operator/map";
import "rxjs/add/operator/catch";
import {TrustStoreService} from "./trustore.service";
import {TrustStoreEntry} from "./trustore.model";
import {TruststoreDialogComponent} from "./truststore-dialog/truststore-dialog.component";
import {MdDialog, MdDialogRef} from "@angular/material";
import {TrustStoreUploadComponent} from "./truststore-upload/truststore-upload.component";

@Component({
  selector: 'app-truststore',
  templateUrl: './truststore.component.html',
  styleUrls: ['./truststore.component.css'],
  providers: [TrustStoreService]
})
export class TruststoreComponent implements OnInit {
  trustStoreEntries: Array<TrustStoreEntry> = [];
  selectedMessages: Array<any> = [];
  loading: boolean = false;

  rows: Array<any> = [];

  constructor(private http: Http, private alertService: AlertService, private trustStoreService: TrustStoreService, public dialog: MdDialog) {
  }

  ngOnInit(): void {
    this.getTrustStoreEntries();
  }

  getTrustStoreEntries(): void {
    this.trustStoreService.getEntries().subscribe(trustStoreEntries => this.trustStoreEntries = trustStoreEntries);
  }

  onSelect({selected}) {
    console.log('Select Event');
    this.selectedMessages.splice(0, this.selectedMessages.length);
    this.selectedMessages.push(...selected);
  }

  onActivate(event) {
    console.log('Activate Event', event);
    if ("dblclick" === event.type) {
      this.details(event.row);
    }
  }

  details(selectedRow: any) {
    let dialogRef: MdDialogRef<TruststoreDialogComponent> = this.dialog.open(TruststoreDialogComponent, {data: {trustStoreEntry: selectedRow}});
    dialogRef.afterClosed().subscribe(result => {

    });
  }

  openEditTrustStore() {
    let dialogRef: MdDialogRef<TrustStoreUploadComponent> = this.dialog.open(TrustStoreUploadComponent);
    dialogRef.afterClosed().subscribe(updated => {
      if (updated == true) {
        this.getTrustStoreEntries();
      }
    });
  }
}
