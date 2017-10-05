import {Component} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {MdDialog} from "@angular/material";
import {PmodeUploadComponent} from "app/pmode-upload/pmode-upload.component";

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import * as FileSaver from "file-saver";
import {isNullOrUndefined} from "util";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";

@Component({
  moduleId: module.id,
  templateUrl: 'pmode.component.html',
  providers: [],
  styleUrls: ['./pmode.component.css']
})

export class PModeComponent {
  private ERROR_PMODE_EMPTY = "As PMode is empty, no file was downloaded.";
  private url = "rest/pmode";

  public pModeExists = false;
  private pModeContents: string = '';

  rows = [];
  selected = [];
  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();
  count: number = 0;
  offset: number = 0;

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  ngOnInit() {
    this.initializeArchivePmodes();
    this.checkPmodeActive();
  }

  initializeArchivePmodes() {
    this.columnPicker.allColumns = [
      {
        name: 'Upload Date'
      },
      {
        name: 'Username'
      },
      {
        name: 'Changes Description'
      },
      {
        name: 'Actions'
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["Upload Date", "Username", "Changes Description", "Actions"].indexOf(col.name) != -1
    });
  }

  onPage(event) {
    console.log('Page Event', event);
    //this.page(event.offset, event.pageSize, this.orderBy, this.asc);
  }

  onSort(event) {
    console.log('Sort Event', event);
    /*let ascending = true;
    if (event.newValue === 'desc') {
      ascending = false;
    }
    this.page(this.offset, this.rowLimiter.pageSize, event.column.prop, ascending);*/
  }

  onSelect({selected}) {
    // console.log('Select Event', selected, this.selected);
  }

  onActivate(event) {
    // console.log('Activate Event', event);

    /*if ("dblclick" === event.type) {
      this.details(event.row);
    }*/
  }

  saveButton() {

  }

  cancelButton() {

  }

  downloadArchive() {

  }

  deleteArchive() {

  }

  rollbackArchive() {

  }

  checkPmodeActive() {
    if(!isNullOrUndefined(this.url)) {
      this.http.get(this.url).subscribe(res => {

        const HTTP_OK = 200;
        if(res.status == HTTP_OK) {
          this.pModeExists = true;
          this.pModeContents = res.text();
        }
      }, err => {
        this.pModeExists = false;
      })
    }
  }

  upload() {
    let dialogRef = this.dialog.open(PmodeUploadComponent);
    dialogRef.componentInstance.onPModeUploaded.subscribe(result => {
      this.checkPmodeActive();
    });
  }

  download() {
    if(this.pModeExists) {
      this.http.get(this.url).subscribe(res => {
        PModeComponent.downloadFile(res.text());
      }, err => {
        this.alertService.error(err._body);
      });
    } else {
      this.alertService.error(this.ERROR_PMODE_EMPTY)
    }

  }

  private static downloadFile(data: any) {
    const blob = new Blob([data], {type: 'text/xml'});
    FileSaver.saveAs(blob, "Pmodes.xml");
  }
}

