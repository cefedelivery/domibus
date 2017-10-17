import {Component, EventEmitter, TemplateRef, ViewChild} from "@angular/core";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {Http, Response} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
import {MdDialog} from "@angular/material";
import {isNullOrUndefined} from "util";
import {PmodeUploadComponent} from "../pmode-upload/pmode-upload.component";
import * as FileSaver from "file-saver";
import {CancelDialogComponent} from "../common/cancel-dialog/cancel-dialog.component";

@Component({
  moduleId: module.id,
  templateUrl: 'pmode.component.html',
  providers: [],
  styleUrls: ['./pmode.component.css']
})

/**
 * PMode Component Typescript
 */
export class PModeComponent {
  private ERROR_PMODE_EMPTY = "As PMode is empty, no file was downloaded.";
  private url = "rest/pmode";

  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;

  loading: boolean = false;

  public pModeExists = false;
  private pModeContents: string = '';

  allPModes = [];
  rows = [];
  selected = [];
  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();
  count: number = 0;
  offset: number = 0;

  disabledSave = true;
  disabledCancel = true;
  disabledDownload = true;
  disabledDelete = true;
  disabledRollback = true;

  actualId: number = 0;
  actualRow: number = 0;

  pModeInformationGot = new EventEmitter(false);

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  /**
   * NgOnInit method
   */
  ngOnInit() {
    this.pModeInformationGot.subscribe(result => {
      this.checkPmodeActive();
      this.page(this.offset, this.rowLimiter.pageSize);
    });
    this.initializeArchivePmodes();
  }

  /**
   * Initialize columns and gets all PMode entries from database
   */
  initializeArchivePmodes() {
    this.columnPicker.allColumns = [
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Configuration Date'
      },
      {
        name: 'Username'
      },
      {
        name: 'Description'
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 80,
        sortable: false
      }
    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["Configuration Date", "Username", "Description", "Actions"].indexOf(col.name) != -1
    });

    this.getAllPModeEntries();
  }

  /**
   *
   * @param {number} newPageLimit
   */
  changePageSize(newPageLimit: number) {
    console.log('New page limit:', newPageLimit);
    this.rowLimiter.pageSize = newPageLimit;
    this.page(0, newPageLimit);
  }

  /**
   *
   * @returns {Subscription}
   */
  getAllPModeEntries() {
    return this.http.get(this.url + "/list").subscribe((response: Response) => {
      this.allPModes = response.json();
      this.count = this.allPModes.length;

      this.allPModes[0].current = true;
      this.actualId = this.allPModes[0].id;
      this.actualRow = 0;
      this.pModeInformationGot.emit();
    });
  }

  /*getPModeEntries(offset: number, pageSize: number): Observable<PModeResult> {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());

    return this.http.get(this.url + "/list", {search:searchParams}).map((response: Response) =>
      response.json()
    );
  }*/

  page(offset, pageSize) {
    this.loading = true;

    this.offset = offset;

    /*let array = this.allPModes.slice();
    array.slice(offset * pageSize, offset * pageSize + pageSize);
    this.rows = array.slice();*/

    this.rows = this.allPModes.slice(offset * pageSize, offset * pageSize + pageSize);
    this.count = this.allPModes.length;

    this.loading = false;

    /*this.getPModeEntries(offset, pageSize).subscribe((result: any) => {
      this.offset = offset;
      this.rowLimiter.pageSize = pageSize;

      const start = offset * pageSize;
      const end = start + pageSize;

      this.count = result.length;

      const newRows = [];

      let index = 0;
      for (let i = start; i < end; i++) {
        newRows[index++] = result[i];
      }

      this.rows = newRows;

      if(offset == 0) {
        this.rows[0].current = true;
        this.rows[0].description = "[CURRENT]: " + this.rows[0].description;
        this.actualId = this.rows[0].id;
        this.pModeInformationGot.emit();
      }

      this.loading = false;

    }, (error: any) => {
        console.log("error getting the PMode Archive:" + error);
        this.loading = false;
        this.alertService.error("Error occured:" + error);
      });*/

/*    this.http.get(this.url + "/list").subscribe( res => {
      this.rows = res.json();
      //this.rows[0].current = true;
      //this.rows[0].description += " (current)";
      this.count = this.rows.length;
    });*/
  }

  /**
   *
   * @param event
   */
  onPage(event) {
    console.log('Page Event', event);
    this.page(event.offset, event.pageSize);
  }

  /**
   *
   * @param event
   */
  onSort(event) {
    console.log('Sort Event', event);
    /*let ascending = true;
    if (event.newValue === 'desc') {
      ascending = false;
    }
    this.page(this.offset, this.rowLimiter.pageSize, event.column.prop, ascending);*/
  }

  /**
   *
   * @param {any} selected
   */
  onSelect({selected}) {
    console.log('Select Event', selected, this.selected);
    if (isNullOrUndefined(selected) || selected.length == 0) {
      this.disabledDownload = true;
      this.disabledDelete = true;
      this.disabledRollback = true;
      return;
    }

    this.disabledDownload = !(this.selected[0] != null && this.selected.length == 1);
    this.disabledDelete = this.selected.findIndex(sel => sel.id === this.actualId) != -1;
    this.disabledRollback = !(this.selected[0] != null && this.selected.length == 1 && this.selected[0].id !== this.actualId);
  }

  /**
   *
   * @param event
   */
  onActivate(event) {
    // console.log('Activate Event', event);

    /*if ("dblclick" === event.type) {
      this.details(event.row);
    }*/
  }

  /**
   *
   */
  saveButton() {
    this.actualId = this.allPModes[0].id;
  }

  /**
   *
   */
  cancelButton() {
    let dialogRef = this.dialog.open(CancelDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.initializeArchivePmodes();
        this.disabledCancel = true;
        this.disabledSave = true;
        this.selected = [];
      }
    });
  }

  /**
   *
   * @param row
   */
  downloadArchive(row) {
    this.download(this.rows[row].id);
  }

  /**
   *
   * @param row
   */
  deleteArchiveAction(row) {

    // workaround to delete one entry from the array
    // since "this.rows.splice(row, 1);" doesn't work...
    let array = this.rows.slice();
    array.splice(row, 1);
    array = array.concat(this.allPModes[this.offset * this.rowLimiter.pageSize + this.rowLimiter.pageSize]);
    this.allPModes.splice(this.offset * this.rowLimiter.pageSize + row, 1);
    this.rows = array.slice();
    this.count--;

    this.disabledSave = false;
    this.disabledCancel = false;
    setTimeout(() => {
      this.selected = [];
    }, 50);
  }

  /**
   *
   */
  deleteArchive() {
    for (let i = this.selected.length - 1; i >= 0; i--) {
      let array = this.rows.slice();
      array.splice(this.selected[i].$$index, 1);
      array = array.concat(this.allPModes[this.offset * this.rowLimiter.pageSize + this.rowLimiter.pageSize]);
      this.allPModes.splice(this.offset * this.rowLimiter.pageSize + this.selected[i].$$index, 1);
      this.rows = array.slice();
      this.count--;
    }

    this.disabledSave = false;
    this.disabledCancel = false;
    this.disabledDelete = true;
    this.selected = [];
  }

  /**
   *
   * @param row
   */
  rollbackArchive(row) {
    //this.rows[row].current = true;
    //this.rows[row].description = "[CURRENT]: " + this.rows[row].description;
    //let array = this.rows.slice();
    /*for(let i = this.rowLimiter.pageSize; i > 0; i--) {
      array[i] = array[i-1];
    }*/
    //array[0].configurationDate = new Date();
    //array[0].username = "admin";
    //array[0].description = "Rolled back to " + this.rows[row].description;
    //this.rows = array;

    //this.actualId = this.rows[row].id;
    //this.actualRow = row;

    this.allPModes[this.actualRow].current = false;
    let elem = {id: row.id,
                configurationDate: new Date(),
                current: true,
                description: "rolledBack " + row.description + " (" + new Date(row.configurationDate).toDateString()  + ")",
                username: "migueti"};
    this.actualRow = 0;

    this.allPModes.unshift(elem);
    this.count++;

    this.page(0, this.rowLimiter.pageSize);

    this.disabledSave = false;
    this.disabledCancel = false;

    setTimeout(() => {
      this.selected = [this.allPModes[0]];
    }, 50);

  }

  /**
   *
   */
  checkPmodeActive() {
    if(!isNullOrUndefined(this.url)) {
      this.http.get(this.url + "/" + this.actualId).subscribe(res => {

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

  /**
   *
   */
  upload() {
    let dialogRef = this.dialog.open(PmodeUploadComponent);
    dialogRef.componentInstance.onPModeUploaded.subscribe(result => {
      this.checkPmodeActive();
    });
  }

  /**
   *
   * @param id
   */
  download(id) {
    if(this.pModeExists) {
      this.http.get(this.url + "/" + id).subscribe(res => {
        PModeComponent.downloadFile(res.text());
      }, err => {
        this.alertService.error(err._body);
      });
    } else {
      this.alertService.error(this.ERROR_PMODE_EMPTY)
    }

  }

  /**
   *
   * @param data
   */
  private static downloadFile(data: any) {
    const blob = new Blob([data], {type: 'text/xml'});
    FileSaver.saveAs(blob, "Pmodes.xml");
  }

  /**
   *
   * @returns {boolean}
   */
  isDirty(): boolean {
    return !this.disabledCancel;
  }
}

