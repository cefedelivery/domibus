import {Component, EventEmitter, TemplateRef, ViewChild} from "@angular/core";
import {ColumnPickerBase} from "../common/column-picker/column-picker-base";
import {RowLimiterBase} from "../common/row-limiter/row-limiter-base";
import {Http, Headers, Response} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
import {MdDialog} from "@angular/material";
import {isNullOrUndefined} from "util";
import {PmodeUploadComponent} from "./pmode-upload/pmode-upload.component";
import * as FileSaver from "file-saver";
import {CancelDialogComponent} from "../common/cancel-dialog/cancel-dialog.component";
import {RollbackDialogComponent} from "app/pmode/rollback-dialog/rollback-dialog.component";
import {SecurityService} from "../security/security.service";
import {SaveDialogComponent} from "../common/save-dialog/save-dialog.component";
import {DirtyOperations} from "../common/dirty-operations";
import {RollbackDirtyDialogComponent} from "./rollback-dirty-dialog/rollback-dirty-dialog.component";
import {PmodeDirtyUploadComponent} from "./pmode-dirty-upload/pmode-dirty-upload.component";
import {Observable} from "rxjs/Observable";

@Component({
  moduleId: module.id,
  templateUrl: 'pmode.component.html',
  providers: [],
  styleUrls: ['./pmode.component.css']
})

/**
 * PMode Component Typescript
 */
export class PModeComponent implements DirtyOperations {
  private ERROR_PMODE_EMPTY = "As PMode is empty, no file was downloaded.";
  private url = "rest/pmode";

  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;


  loading: boolean = false;

  public pModeExists = false;
  private pModeContents: string = '';

  allPModes = [];
  tableRows = [];
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

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog,
              private securityService: SecurityService) {
  }

  /**
   * NgOnInit method
   */
  ngOnInit() {
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
   * Change
   * @param {number} newPageLimit
   */
  changePageSize(newPageLimit: number) {
    console.log('New page limit:', newPageLimit);
    this.rowLimiter.pageSize = newPageLimit;
    this.page(0, newPageLimit);
  }

  /**
   *
   * @returns {Observable<Response>}
   */
  getResultObservable():Observable<Response>{
    return this.http.get(this.url + "/list")
    .publishReplay(1).refCount();
  }

  /**
   * Gets all the PModes Entries
   */
  getAllPModeEntries() {
    this.getResultObservable().subscribe((response: Response) => {
      this.allPModes = response.json();
      this.allPModes[0].current = true;
      this.actualId = this.allPModes[0].id;
      this.checkPmodeActive();
      this.actualRow = 0;
      this.count = response.json().length;
    },
      () => {},
      () => {
        this.tableRows = this.allPModes.slice(this.offset, this.rowLimiter.pageSize);
        this.tableRows[0].current = true;
        this.tableRows[0].description = "[CURRENT]: " + this.allPModes[0].description;
      });
  }

  page(offset, pageSize) {
    this.loading = true;

    this.offset = offset;
    this.rowLimiter.pageSize = pageSize;

    this.tableRows = this.allPModes.slice(this.offset * this.rowLimiter.pageSize, (this.offset + 1) * this.rowLimiter.pageSize);

    this.loading = false;
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
   * @param {any} selected
   */
  onSelect({selected}) {
    console.log('Select Event', selected, this.selected);
    if (isNullOrUndefined(selected) || selected.length == 0) {
      this.disabledSave = true;
      this.disabledCancel = true;
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
  }

  /**
   *
   */
  saveButton() {
    let headers = new Headers({'Content-Type': 'application/json'});
    let dialogRef = this.dialog.open(SaveDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.http.put(this.url + "/addAll", JSON.stringify(this.allPModes), {headers: headers}).subscribe(result => {
            this.alertService.success("The operation 'update pmodes' completed successfully.", false);
            this.disabledSave = true;
            this.disabledCancel = true;
            this.disabledDownload = true;
            this.disabledDelete = true;
            this.disabledRollback = true;
            this.selected = [];
          },
          error => {
            this.alertService.error("The operation 'update pmodes' not completed successfully.", false);
            this.disabledSave = false;
            this.disabledCancel = false;
            this.disabledDownload = true;
            this.disabledDelete = true;
            this.disabledRollback = true;
            this.selected = [];
          });
      }
    });
  }

  /**
   *
   */
  cancelButton() {
    let dialogRef = this.dialog.open(CancelDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.initializeArchivePmodes();
        this.disabledSave = true;
        this.disabledCancel = true;
      } else {
        this.disabledSave = false;
        this.disabledCancel = false;
      }
    });
    this.disabledDownload = true;
    this.disabledDelete = true;
    this.disabledRollback = true;
    this.selected = [];
  }

  /**
   *
   * @param row
   */
  downloadArchive(row) {
    this.download(this.tableRows[row].id);
  }

  /**
   *
   * @param row
   */
  deleteArchiveAction(row) {

    // workaround to delete one entry from the array
    // since "this.rows.splice(row, 1);" doesn't work...
    let array = this.tableRows.slice();
    array.splice(row, 1);
    array = array.concat(this.allPModes[this.offset * this.rowLimiter.pageSize + this.rowLimiter.pageSize]);
    this.allPModes.splice(this.offset * this.rowLimiter.pageSize + row, 1);
    this.tableRows = array.slice();
    this.count--;

    setTimeout(() => {
      this.selected = [];
      this.disabledSave = false;
      this.disabledCancel = false;
      this.disabledDownload = true;
      this.disabledDelete = true;
      this.disabledRollback = true;
    }, 100);


  }

  /**
   *
   */
  deleteArchive() {
    for (let i = this.selected.length - 1; i >= 0; i--) {
      let array = this.tableRows.slice();
      array.splice(this.selected[i].$$index, 1);
      array = array.concat(this.allPModes[this.offset * this.rowLimiter.pageSize + this.rowLimiter.pageSize]);
      this.allPModes.splice(this.offset * this.rowLimiter.pageSize + this.selected[i].$$index, 1);
      this.tableRows = array.slice();
      this.count--;
    }

    this.disabledSave = false;
    this.disabledCancel = false;
    this.disabledDownload = true;
    this.disabledDelete = true;
    this.disabledRollback = true;
    this.selected = [];
  }

  /**
   * Rollbacks the PMode for the selected row
   * - Creates a similar entry like @param selectedRow
   * - Sets that entry as current
   *
   * @param selectedRow Selected Row
   */
  rollbackArchive(selectedRow) {
    if (!this.isDirty()) {
      let dialogRef = this.dialog.open(RollbackDialogComponent);
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.allPModes[this.actualRow].current = false;
          let input = new FormData();
          input.append('rolledbackId', selectedRow.id);
          this.http.post(this.url + "/add", input).subscribe(res => {
            this.actualRow = 0;

            this.getAllPModeEntries();

            this.disabledDelete = true;
            this.disabledRollback = true;
            this.disabledDownload = true;
            this.selected = [];

          });
        }
      });
    } else {
      let dialogRef = this.dialog.open(RollbackDirtyDialogComponent);
      dialogRef.afterClosed().subscribe(result => {
        if (result === 'first') {
          let headers = new Headers({'Content-Type': 'application/json'});
          this.http.put(this.url + "/addAll", JSON.stringify(this.allPModes), {headers: headers}).subscribe(result => {
              this.disabledSave = true;
              this.disabledCancel = true;
              this.disabledDownload = true;
              this.disabledDelete = true;
              this.disabledRollback = true;
              this.selected = [];
              this.allPModes[this.actualRow].current = false;
              let input = new FormData();
              input.append('rolledbackId', selectedRow.id);
              this.http.post(this.url + "/add", input).subscribe(res => {
                this.actualRow = 0;
                this.getAllPModeEntries();
              });
            },
            error => {
              this.alertService.error("The operation 'update pmodes' not completed successfully.", false);
              this.disabledSave = false;
              this.disabledCancel = false;
              this.disabledDownload = true;
              this.disabledDelete = true;
              this.disabledRollback = true;
              this.selected = [];
            });
        } else if (result === 'third') {
          this.allPModes[this.actualRow].current = false;
          let input = new FormData();
          input.append('rolledbackId', selectedRow.id);
          this.http.post(this.url + "/add", input).subscribe(res => {
            this.actualRow = 0;

            /*setTimeout(() => {
              this.getAllPModeEntries();
            }, 100);*/
            this.getAllPModeEntries();
          });
        }
        this.disabledDelete = true;
        this.disabledRollback = true;
        this.disabledDownload = true;
        this.selected = [];

      });
    }
  }

  /**
   *
   */
  checkPmodeActive() {
    if (!isNullOrUndefined(this.url)) {
      this.http.get(this.url + "/" + this.actualId).subscribe(res => {

        const HTTP_OK = 200;
        if (res.status == HTTP_OK) {
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
    if (this.isDirty()) {
      let dialogRef = this.dialog.open(PmodeDirtyUploadComponent);
      dialogRef.afterClosed().subscribe(result => {
        if (result === 'first') {
          let headers = new Headers({'Content-Type': 'application/json'});
          this.http.put(this.url + "/addAll", JSON.stringify(this.allPModes), {headers: headers}).subscribe(result => {
              this.disabledSave = true;
              this.disabledCancel = true;
              this.disabledDownload = true;
              this.disabledDelete = true;
              this.disabledRollback = true;
              this.selected = [];

              let dialogRef = this.dialog.open(PmodeUploadComponent);
              dialogRef.afterClosed().subscribe(result => {
                this.getAllPModeEntries();
              });
            },
            error => {
              this.alertService.error("The operation 'update pmodes' not completed successfully.", false);
              this.disabledSave = false;
              this.disabledCancel = false;
              this.disabledDownload = true;
              this.disabledDelete = true;
              this.disabledRollback = true;
              this.selected = [];
            });
        } else if (result === 'third') {
          let dialogRef = this.dialog.open(PmodeUploadComponent);
          dialogRef.afterClosed().subscribe(result => {
            this.getAllPModeEntries();
          });
        }
      });
    } else {
      let dialogRef = this.dialog.open(PmodeUploadComponent);
      dialogRef.afterClosed().subscribe(result => {
        this.getAllPModeEntries();
      });
    }
  }

  /**
   *
   * @param id
   */
  download(id) {
    if (this.pModeExists) {
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

  getPModeInformation() {
    this.getResultObservable().
    map((response: Response) => response.json()).
    map((response)=>response.slice(this.offset * this.rowLimiter.pageSize, (this.offset + 1) * this.rowLimiter.pageSize)).
    subscribe((response) => {
        this.tableRows = response;
        if(this.offset == 0) {
          this.tableRows[0].current = true;
          this.tableRows[0].description = "[CURRENT]: " + response[0].description;
        }
      }, () => {
      },
      () => {
        this.allPModes[0].current = true;
        this.actualId = this.allPModes[0].id;
        this.actualRow = 0;
        this.count = this.allPModes.length;
      });
  }

  /**
   * Method called every time a tab changes
   * @param value Tab Position
   */
  selectedIndexChange(value){
    console.log(value);
    if(value==1) {
      this.getPModeInformation();
    }
  }
}

