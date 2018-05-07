import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ColumnPickerBase} from 'app/common/column-picker/column-picker-base';
import {RowLimiterBase} from 'app/common/row-limiter/row-limiter-base';
import {Http, Headers, Response} from '@angular/http';
import {AlertService} from 'app/alert/alert.service';
import {MdDialog} from '@angular/material';
import {isNullOrUndefined} from 'util';
import {PmodeUploadComponent} from '../pmode-upload/pmode-upload.component';
import * as FileSaver from 'file-saver';
import {CancelDialogComponent} from 'app/common/cancel-dialog/cancel-dialog.component';
import {RollbackDialogComponent} from 'app/pmode/rollback-dialog/rollback-dialog.component';
import {SaveDialogComponent} from 'app/common/save-dialog/save-dialog.component';
import {DirtyOperations} from 'app/common/dirty-operations';
import {RollbackDirtyDialogComponent} from '../rollback-dirty-dialog/rollback-dirty-dialog.component';
import {PmodeDirtyUploadComponent} from '../pmode-dirty-upload/pmode-dirty-upload.component';
import {Observable} from 'rxjs/Observable';
import {DateFormatService} from 'app/customDate/dateformat.service';
import {DownloadService} from 'app/download/download.service';
import {AlertComponent} from 'app/alert/alert.component';
import {ActionDirtyDialogComponent} from '../action-dirty-dialog/action-dirty-dialog.component';

@Component({
  moduleId: module.id,
  templateUrl: 'currentPMode.component.html',
  providers: [],
  styleUrls: ['./currentPMode.component.css']
})

/**
 * PMode Component Typescript
 */
export class CurrentPModeComponent implements OnInit, DirtyOperations {
  static readonly PMODE_URL: string = 'rest/pmode';
  static readonly PMODE_CSV_URL: string = CurrentPModeComponent.PMODE_URL + '/csv';

  private ERROR_PMODE_EMPTY = 'As PMode is empty, no file was downloaded.';

  // @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;
  // @ViewChild('rowActions') rowActions: TemplateRef<any>;

  // loading = false;

  public pModeExists = false;
  private pModeContents = '';
  private pModeContentsDirty = false;

  // allPModes = [];
  // tableRows = [];
  // selected = [];
  // columnPicker: ColumnPickerBase = new ColumnPickerBase();
  // rowLimiter: RowLimiterBase = new RowLimiterBase();
  // count: number = 0;
  // offset: number = 0;

  // disabledSave = true;
  // disabledCancel = true;
  // disabledDownload = true;
  // disabledDelete = true;
  // disabledRollback = true;

  current: any;
  // actualId = 0;
  // actualRow: number = 0;

  deleteList = [];

  // needed for the first request after upload
  // datatable was empty if we don't do the request again
  // resize window shows information
  // check: @selectedIndexChange(value)
  private uploaded = false;

  private headers = new Headers({'Content-Type': 'application/json'});

  /**
   * Constructor
   * @param {Http} http Http object used for the requests
   * @param {AlertService} alertService Alert Service object used for alerting success and error messages
   * @param {MdDialog} dialog Object used for opening dialogs
   */
  constructor (private http: Http, private alertService: AlertService, public dialog: MdDialog) {
  }

  /**
   * NgOnInit method
   */
  ngOnInit () {
    this.getAllPModeEntries();
    //this.initializeArchivePmodes();
  }

  /**
   * Initialize columns and gets all PMode entries from database
   */
  // initializeArchivePmodes() {
  //   this.columnPicker.allColumns = [
  //     {
  //       cellTemplate: this.rowWithDateFormatTpl,
  //       name: 'Configuration Date'
  //     },
  //     {
  //       name: 'Username'
  //     },
  //     {
  //       name: 'Description'
  //     },
  //     {
  //       cellTemplate: this.rowActions,
  //       name: 'Actions',
  //       width: 80,
  //       sortable: false
  //     }
  //   ];
  //
  //   this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
  //     return ['Configuration Date', 'Username', 'Description', 'Actions'].indexOf(col.name) != -1
  //   });
  //
  //   this.getAllPModeEntries();
  // }

  /**
   * Change Page size for a @newPageLimit value
   * @param {number} newPageLimit New value for page limit
   */
  // changePageSize(newPageLimit: number) {
  //   console.log('New page limit:', newPageLimit);
  //   this.rowLimiter.pageSize = newPageLimit;
  //   this.page(0, newPageLimit);
  // }

  /**
   * Gets all the PMode
   * @returns {Observable<Response>}
   */
  getResultObservable (): Observable<Response> {
    return this.http.get(CurrentPModeComponent.PMODE_URL + '/list')
      .publishReplay(1).refCount();
  }

  /**
   * Gets all the PModes Entries
   */
  getAllPModeEntries () {
    this.getResultObservable().subscribe((response: Response) => {
        // this.allPModes = response.json();
        // this.allPModes[0].current = true;
        // this.actualId = this.allPModes[0].id;

        this.current = response.json()[0];
        // this.actualId = this.current.id;
        console.log('this.current', this.current);
        this.getActivePMode();
        // this.actualRow = 0;
        // this.count = response.json().length;
        // if (this.count > AlertComponent.MAX_COUNT_CSV) {
        //   this.alertService.error('Maximum number of rows reached for downloading CSV');
        // }
      },
      () => {
      },
      () => {
        // this.tableRows = this.allPModes.slice(0, this.rowLimiter.pageSize);
        // this.tableRows[0].current = true;
        // this.tableRows[0].description = '[CURRENT]: ' + this.allPModes[0].description;
      });
  }

  /**
   *
   * @param offset
   * @param pageSize
   */
  // page(offset, pageSize) {
  //   this.loading = true;
  //
  //   this.offset = offset;
  //   this.rowLimiter.pageSize = pageSize;
  //
  //   this.tableRows = this.allPModes.slice(this.offset * this.rowLimiter.pageSize, (this.offset + 1) * this.rowLimiter.pageSize);
  //
  //   this.loading = false;
  // }

  /**
   *
   * @param event
   */
  // onPage(event) {
  //   console.log('Page Event', event);
  //   this.page(event.offset, event.pageSize);
  // }

  /**
   * Disable All the Buttons
   * used mainly when no row is selected
   */
  // private disableAllButtons () {
  //   this.disabledSave = true;
  //   this.disabledCancel = true;
  //   this.disabledDownload = true;
  //   this.disabledDelete = true;
  //   this.disabledRollback = true;
  // }

  /**
   * Enable Save and Cancel buttons
   * used when changes occurred (deleted entries)
   */
  // private enableSaveAndCancelButtons () {
  //   this.disabledSave = false;
  //   this.disabledCancel = false;
  //   this.disabledDownload = true;
  //   this.disabledDelete = true;
  //   this.disabledRollback = true;
  // }

  /**
   * Method called by NgxDatatable on selection/deselection
   * @param {any} selected selected/unselected object
   */
  // onSelect({selected}) {
  //   console.log('Select Event', selected, this.selected);
  //   if (isNullOrUndefined(selected) || selected.length == 0) {
  //     this.disableAllButtons();
  //     return;
  //   }
  //
  //   this.disabledDownload = !(this.selected[0] != null && this.selected.length == 1);
  //   this.disabledDelete = this.selected.findIndex(sel => sel.id === this.actualId) != -1;
  //   this.disabledRollback = !(this.selected[0] != null && this.selected.length == 1 && this.selected[0].id !== this.actualId);
  // }

  /**
   * Method used when button save is clicked
   */
  // saveButton (withDownloadCSV: boolean) {
  //   this.dialog.open(SaveDialogComponent).afterClosed().subscribe(result => {
  //     if (result) {
  //       this.http.delete(CurrentPModeComponent.PMODE_URL, {params: {ids: JSON.stringify(this.deleteList)}}).subscribe(() => {
  //           this.alertService.success('The operation \'update pmodes\' completed successfully.', false);
  //           this.disableAllButtons();
  //           // this.selected = [];
  //           this.deleteList = [];
  //           if (withDownloadCSV) {
  //             DownloadService.downloadNative(CurrentPModeComponent.PMODE_CSV_URL);
  //           }
  //         },
  //         () => {
  //           this.alertService.error('The operation \'update pmodes\' not completed successfully.', false);
  //           this.getAllPModeEntries();
  //           this.disableAllButtons();
  //           // this.selected = [];
  //         });
  //     } else {
  //       if (withDownloadCSV) {
  //         DownloadService.downloadNative(CurrentPModeComponent.PMODE_CSV_URL);
  //       }
  //     }
  //   });
  // }

  /**
   * Method used when Cancel button is clicked
   */
  // cancelButton () {
  //   let dialogRef = this.dialog.open(CancelDialogComponent);
  //   dialogRef.afterClosed().subscribe(result => {
  //     if (result) {
  //       this.deleteList = [];
  //       //this.initializeArchivePmodes();
  //       this.disabledSave = true;
  //       this.disabledCancel = true;
  //     } else {
  //       this.disabledSave = false;
  //       this.disabledCancel = false;
  //     }
  //   });
  //   this.disabledDownload = true;
  //   this.disabledDelete = true;
  //   this.disabledRollback = true;
  //   // this.selected = [];
  // }

  /**
   * Method called when Download button is clicked
   * @param row The selected row
   */
  // downloadArchive(row) {
  //   this.download(this.tableRows[row].id);
  // }

  /**
   * Method called when Action Delete icon is clicked
   * @param row Row where Delete icon is located
   */
  // deleteArchiveAction(row) {
  //
  //   // workaround to delete one entry from the array
  //   // since "this.rows.splice(row, 1);" doesn't work...
  //   let array = this.tableRows.slice();
  //   this.deleteList.push(array[row].id);
  //   array.splice(row, 1);
  //   array = array.concat(this.allPModes[this.offset * this.rowLimiter.pageSize + this.rowLimiter.pageSize]);
  //   this.allPModes.splice(this.offset * this.rowLimiter.pageSize + row, 1);
  //   this.tableRows = array.slice();
  //   this.count--;
  //
  //   if (this.offset > 0 && this.isPageEmpty()) {
  //     this.page(this.offset - 1, this.rowLimiter.pageSize);
  //   }
  //
  //   setTimeout(() => {
  //     this.selected = [];
  //     this.enableSaveAndCancelButtons();
  //   }, 100);
  // }

  /**
   * Method called when Delete button is clicked
   * All the selected rows will be deleted
   */
  // deleteArchive() {
  //   for (let i = this.selected.length - 1; i >= 0; i--) {
  //     let array = this.tableRows.slice();
  //     array.splice(this.selected[i].$$index, 1);
  //     array = array.concat(this.allPModes[this.offset * this.rowLimiter.pageSize + this.rowLimiter.pageSize]);
  //     this.allPModes.splice(this.offset * this.rowLimiter.pageSize + this.selected[i].$$index, 1);
  //     this.tableRows = array.slice();
  //     this.deleteList.push(this.selected[i].id);
  //     this.count--;
  //   }
  //
  //   if (this.offset > 0 && this.isPageEmpty()) {
  //     this.page(this.offset - 1, this.rowLimiter.pageSize);
  //   }
  //
  //   this.enableSaveAndCancelButtons();
  //   this.selected = [];
  // }

  /**
   * Method return true if all elements this.tableRows
   * are null or undefined or if is empty.
   *
   */
  // isPageEmpty(): boolean {
  //   if (this.tableRows || this.tableRows.length) {
  //     for (let i = 0; i < this.tableRows.length; i++) {
  //       if (this.tableRows[i]) {
  //         return false;
  //       }
  //     }
  //   }
  //   return true;
  // }

  /**
   * Method called when Rollback button is clicked
   * Rollbacks the PMode for the selected row
   * - Creates a similar entry like @selectedRow
   * - Sets that entry as current
   *
   * @param selectedRow Selected Row
   */
  // rollbackArchive(selectedRow) {
  //   if (!this.isDirty()) {
  //     let dialogRef = this.dialog.open(RollbackDialogComponent);
  //     dialogRef.afterClosed().subscribe(result => {
  //       if (result) {
  //         this.allPModes[this.actualRow].current = false;
  //         this.http.put(CurrentPModeComponent.PMODE_URL + '/rollback/' + selectedRow.id, null, {headers: this.headers}).subscribe(res => {
  //           this.actualRow = 0;
  //
  //           this.getAllPModeEntries();
  //
  //           this.disableAllButtons();
  //           this.selected = [];
  //         });
  //       }
  //     });
  //   } else {
  //     let dialogRef = this.dialog.open(RollbackDirtyDialogComponent);
  //     dialogRef.afterClosed().subscribe(result => {
  //       if (result === 'ok') {
  //         this.http.delete(CurrentPModeComponent.PMODE_URL, {params: {ids: JSON.stringify(this.deleteList)}}).subscribe(result => {
  //             this.deleteList = [];
  //             this.disableAllButtons();
  //             this.selected = [];
  //
  //             this.allPModes[this.actualRow].current = false;
  //             this.http.put(CurrentPModeComponent.PMODE_URL + '/rollback/' + selectedRow.id, null, {headers: this.headers}).subscribe(res => {
  //               this.actualRow = 0;
  //               this.getAllPModeEntries();
  //             });
  //           },
  //           error => {
  //             this.alertService.error('The operation \'update pmodes\' not completed successfully.', false);
  //             this.enableSaveAndCancelButtons();
  //             this.selected = [];
  //           });
  //       } else if (result === 'rollback_only') {
  //         this.deleteList = [];
  //         this.allPModes[this.actualRow].current = false;
  //         this.http.put(CurrentPModeComponent.PMODE_URL + '/rollback/' + selectedRow.id, null, {headers: this.headers}).subscribe(res => {
  //           this.actualRow = 0;
  //           this.getAllPModeEntries();
  //         });
  //         this.disableAllButtons();
  //       }
  //       this.selected = [];
  //     });
  //   }
  //   this.page(0, this.rowLimiter.pageSize);
  // }

  /**
   * Get Request for the Active PMode XML
   */
  getActivePMode () {
    if (!isNullOrUndefined(CurrentPModeComponent.PMODE_URL)) {
      this.pModeContentsDirty = false;
      this.http.get(CurrentPModeComponent.PMODE_URL + '/' + this.current.id + '?noAudit=true ').subscribe(res => {
        const HTTP_OK = 200;
        if (res.status === HTTP_OK) {
          this.pModeExists = true;
          this.pModeContents = res.text();
        }
      }, err => {
        this.pModeExists = false;
      })
    }
  }

  /**
   * Method called when Upload button is clicked
   */
  upload () {
    if (this.isDirty()) {
      this.dialog.open(PmodeDirtyUploadComponent).afterClosed().subscribe(result => {
        if (result === 'ok') {
          this.http.delete(CurrentPModeComponent.PMODE_URL, {params: {ids: JSON.stringify(this.deleteList)}}).subscribe(result => {
              // this.deleteList = [];
              // this.disableAllButtons();
              // this.selected = [];

              this.uploadPmode();
            },
            error => {
              this.alertService.error('The operation \'update pmodes\' not completed successfully.', false);
              // this.enableSaveAndCancelButtons();
              // this.selected = [];
            });
        } else if (result === 'upload_only') {
          // this.deleteList = [];

          this.uploadPmode();
        }
      });
    } else {
      this.uploadPmode();
    }
  }

  private uploadPmode () {
    this.dialog.open(PmodeUploadComponent)
      .afterClosed().subscribe(result => {
      this.getAllPModeEntries();
    });
    this.uploaded = true;
  }

  /**
   * Method called when Download button or icon is clicked
   * @param id The id of the selected entry on the DB
   */
  download (pmode) {
    if (this.pModeExists) {
      this.http.get(CurrentPModeComponent.PMODE_URL + '/' + pmode.id).subscribe(res => {
        const uploadDateStr = DateFormatService.format(new Date(pmode.configurationDate));
        CurrentPModeComponent.downloadFile(res.text(), uploadDateStr);
      }, err => {
        this.alertService.error(err._body);
      });
    } else {
      this.alertService.error(this.ERROR_PMODE_EMPTY)
    }
  }

  /**
   * Method called when 'Save' button is clicked
   */
  save () {
    // if (this.isDirty()) {
    //   this.dialog.open(ActionDirtyDialogComponent, {
    //     data: {
    //       actionTitle: 'You will now save the current PMode',
    //       actionName: 'save',
    //       actionIconName: 'file_upload'
    //     }
    //   }).afterClosed().subscribe(response => {
    //     if (response === 'ok') {
    //       this.http.delete(CurrentPModeComponent.PMODE_URL, {params: {ids: JSON.stringify(this.deleteList)}}).subscribe(result => {
    //           // this.deleteList = [];
    //           // this.disableAllButtons();
    //           // this.selected = [];
    //
    //           this.uploadPmodeContent();
    //         },
    //         error => {
    //           this.alertService.error('The operation \'update pmodes\' not completed successfully.', false);
    //           // this.enableSaveAndCancelButtons();
    //           // this.selected = [];
    //         });
    //     } else if (response === 'save') {
    //       this.uploadPmodeContent();
    //     }
    //   });
    // } else {
      this.uploadPmodeContent();
    // }
  }

  private uploadPmodeContent () {
    this.dialog.open(PmodeUploadComponent, {
      data: {pModeContents: this.pModeContents}
    }).afterClosed().subscribe(result => {
      if (result && result.done) {
        this.uploaded = true;
        this.getAllPModeEntries();
      }
    });
  }

  /**
   * Method called when 'Cancel' button is clicked
   */
  cancel () {
    this.dialog.open(CancelDialogComponent)
      .afterClosed().subscribe(response => {
      if (response) {
        this.getActivePMode();
      }
    });
  }

  /**
   * Method that checks if 'Save' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canSave (): boolean {
    return this.pModeExists && this.pModeContentsDirty;
  }

  /**
   * Method that checks if 'Cancel' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canCancel (): boolean {
    return this.pModeExists && this.pModeContentsDirty;
  }

  /**
   * Method that checks if 'Download' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canDownload (): boolean {
    return this.pModeExists && !this.pModeContentsDirty;
  }

  /**
   * Method called when the pmode text is changed by the user
   */
  textChanged () {
    this.pModeContentsDirty = true;
  }

  /**
   * Method that checks if CSV Button export can be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  // isSaveAsCSVButtonEnabled(): boolean {
  //   return this.allPModes.length < AlertComponent.MAX_COUNT_CSV;
  // }

  /**
   * Saves the content of the datatable into a CSV file
   */
  // saveAsCSV () {
  //   if (this.isDirty()) {
  //     this.saveButton(true);
  //   } else {
  //     DownloadService.downloadNative(CurrentPModeComponent.PMODE_CSV_URL);
  //   }
  // }

  /**
   * Downloader for the XML file
   * @param data
   */
  private static downloadFile (data: any, date: string) {
    const blob = new Blob([data], {type: 'text/xml'});
    let filename = 'PMode';
    if (date !== '') {
      filename += '-' + date;
    }
    filename += '.xml';
    FileSaver.saveAs(blob, filename);
  }

  /**
   * IsDirty method used for the IsDirtyOperations
   * @returns {boolean}
   */
  isDirty (): boolean {
    return this.canCancel();
    //return !this.disabledCancel;
  }

  /**
   * Method called every time a tab changes
   * @param value Tab Position
   */
  // selectedIndexChange(value) {
  //   if (value == 1 && this.uploaded) { // Archive Tab
  //     this.getResultObservable().map((response: Response) => response.json()).map((response) => response.slice(this.offset * this.rowLimiter.pageSize, (this.offset + 1) * this.rowLimiter.pageSize)).subscribe((response) => {
  //         this.tableRows = response;
  //         if (this.offset == 0) {
  //           this.tableRows[0].current = true;
  //           this.tableRows[0].description = '[CURRENT]: ' + response[0].description;
  //         }
  //         this.uploaded = false;
  //       }, () => {
  //       },
  //       () => {
  //         this.allPModes[0].current = true;
  //         this.actualId = this.allPModes[0].id;
  //         this.actualRow = 0;
  //         this.count = this.allPModes.length;
  //       });
  //   }
  // }

}

