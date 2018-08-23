import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {TrustStoreService} from './trustore.service';
import {TrustStoreEntry} from './trustore.model';
import {TruststoreDialogComponent} from './truststore-dialog/truststore-dialog.component';
import {MdDialog, MdDialogRef} from '@angular/material';
import {TrustStoreUploadComponent} from './truststore-upload/truststore-upload.component';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {DownloadService} from '../download/download.service';
import {AlertComponent} from '../alert/alert.component';
import {AlertService} from '../alert/alert.service';

@Component({
  selector: 'app-truststore',
  templateUrl: './truststore.component.html',
  styleUrls: ['./truststore.component.css'],
  providers: [TrustStoreService]
})
export class TruststoreComponent implements OnInit {
  static readonly TRUSTSTORE_URL: string = 'rest/truststore';
  static readonly TRUSTSTORE_CSV_URL: string = TruststoreComponent.TRUSTSTORE_URL + '/csv';

  columnPicker: ColumnPickerBase = new ColumnPickerBase();

  rowLimiter: RowLimiterBase = new RowLimiterBase();

  @ViewChild('rowWithDateFormatTpl') rowWithDateFormatTpl: TemplateRef<any>;

  trustStoreEntries: Array<TrustStoreEntry>;
  selectedMessages: Array<any>;
  loading: boolean;

  rows: Array<any> = [];
  offset: number;

  constructor (private trustStoreService: TrustStoreService, public dialog: MdDialog, public alertService: AlertService) {
  }

  ngOnInit (): void {
    this.columnPicker.allColumns = [
      {

        name: 'Name',
        prop: 'name'
      },
      {
        name: 'Subject',
        prop: 'subject',
      },
      {
        name: 'Issuer',
        prop: 'issuer',
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Valid from',
        prop: 'validFrom'

      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Valid until',
        prop: 'validUntil',
      }

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Name', 'Subject', 'Issuer', 'Valid from', 'Valid until'].indexOf(col.name) !== -1
    });

    this.trustStoreEntries = [];
    this.selectedMessages = [];
    this.rows = [];

    this.offset = 0;

    this.getTrustStoreEntries();
  }

  getTrustStoreEntries (): void {
    this.trustStoreService.getEntries().subscribe(trustStoreEntries => {
      this.trustStoreEntries = trustStoreEntries;
      this.offset = 0;
    });
  }

  onSelect ({selected}) {
    this.selectedMessages.splice(0, this.selectedMessages.length);
    this.selectedMessages.push(...selected);
  }

  onActivate (event) {
    if ('dblclick' === event.type) {
      this.details(event.row);
    }
  }

  details (selectedRow: any) {
    this.dialog.open(TruststoreDialogComponent, {data: {trustStoreEntry: selectedRow}})
      .afterClosed().subscribe(result => {
    });
  }

  onChangePage (event: any): void {
    this.offset = event.offset;
  }

  changePageSize (newPageSize: number) {
    this.rowLimiter.pageSize = newPageSize;
    this.getTrustStoreEntries();
  }

  openEditTrustStore () {
    this.dialog.open(TrustStoreUploadComponent).componentInstance.onTruststoreUploaded
      .subscribe(updated => {
        this.getTrustStoreEntries();
      });
  }

  /**
   * Saves the content of the datatable into a CSV file
   */
  saveAsCSV () {
    if (this.trustStoreEntries.length > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
      return;
    }

    DownloadService.downloadNative(TruststoreComponent.TRUSTSTORE_CSV_URL);
  }

}
