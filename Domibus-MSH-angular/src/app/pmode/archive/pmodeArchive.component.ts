import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ColumnPickerBase} from 'app/common/column-picker/column-picker-base';
import {RowLimiterBase} from 'app/common/row-limiter/row-limiter-base';
import {Http, Headers, Response} from '@angular/http';
import {AlertService} from 'app/alert/alert.service';
import {MdDialog, MdDialogRef} from '@angular/material';
import {isNullOrUndefined} from 'util';
import {PmodeUploadComponent} from '../pmode-upload/pmode-upload.component';
import * as FileSaver from 'file-saver';
import {CancelDialogComponent} from 'app/common/cancel-dialog/cancel-dialog.component';
import {ActionDirtyDialogComponent} from 'app/pmode/action-dirty-dialog/action-dirty-dialog.component';
import {SaveDialogComponent} from 'app/common/save-dialog/save-dialog.component';
import {DirtyOperations} from 'app/common/dirty-operations';
import {Observable} from 'rxjs/Observable';
import {DateFormatService} from 'app/customDate/dateformat.service';
import {DownloadService} from 'app/download/download.service';
import {AlertComponent} from 'app/alert/alert.component';
import {RestoreDialogComponent} from '../restore-dialog/restore-dialog.component';
import {PmodeViewComponent} from './pmode-view/pmode-view.component';
import {CurrentPModeComponent} from '../current/currentPMode.component';

@Component({
  moduleId: module.id,
  templateUrl: 'pmodeArchive.component.html',
  providers: [],
  styleUrls: ['./pmodeArchive.component.css']
})

/**
 * PMode Component Typescript
 */
export class PModeArchiveComponent implements OnInit, DirtyOperations {
  static readonly PMODE_URL: string = 'rest/pmode';
  static readonly PMODE_CSV_URL: string = PModeArchiveComponent.PMODE_URL + '/csv';

  private ERROR_PMODE_EMPTY = 'As PMode is empty, no file was downloaded.';

  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;

  loading = false;

  allPModes = [];
  tableRows = [];
  selected = [];
  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();
  count = 0;
  offset = 0;

  disabledSave = true;
  disabledCancel = true;
  disabledDownload = true;
  disabledDelete = true;
  disabledRestore = true;

  actualId = 0;
  actualRow = 0;

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
    this.initializeArchivePmodes();
  }

  /**
   * Initialize columns and gets all PMode entries from database
   */
  initializeArchivePmodes () {
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
      return ['Configuration Date', 'Username', 'Description', 'Actions'].indexOf(col.name) !== -1
    });

    this.getAllPModeEntries();
  }

  /**
   * Change Page size for a @newPageLimit value
   * @param {number} newPageLimit New value for page limit
   */
  changePageSize (newPageLimit: number) {
    console.log('New page limit:', newPageLimit);
    this.rowLimiter.pageSize = newPageLimit;
    this.page(0, newPageLimit);
  }

  /**
   * Gets all the PMode
   * @returns {Observable<Response>}
   */
  getResultObservable (): Observable<Response> {
    return this.http.get(PModeArchiveComponent.PMODE_URL + '/list')
      .publishReplay(1).refCount();
  }

  /**
   * Gets all the PModes Entries
   */
  getAllPModeEntries () {
    this.getResultObservable().subscribe((response: Response) => {
        this.allPModes = response.json();
        this.allPModes[0].current = true;
        this.actualId = this.allPModes[0].id;
        this.actualRow = 0;
        this.count = response.json().length;
        if (this.count > AlertComponent.MAX_COUNT_CSV) {
          this.alertService.error('Maximum number of rows reached for downloading CSV');
        }
      },
      () => {
      },
      () => {
        this.tableRows = this.allPModes.slice(0, this.rowLimiter.pageSize);
        this.tableRows[0].current = true;
        this.tableRows[0].description = '[CURRENT]: ' + this.allPModes[0].description;
      });
  }

  /**
   *
   * @param offset
   * @param pageSize
   */
  page (offset, pageSize) {
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
  onPage (event) {
    console.log('Page Event', event);
    this.page(event.offset, event.pageSize);
  }

  /**
   * Disable All the Buttons
   * used mainly when no row is selected
   */
  private disableAllButtons () {
    this.disabledSave = true;
    this.disabledCancel = true;
    this.disabledDownload = true;
    this.disabledDelete = true;
    this.disabledRestore = true;
  }

  /**
   * Enable Save and Cancel buttons
   * used when changes occurred (deleted entries)
   */
  private enableSaveAndCancelButtons () {
    this.disabledSave = false;
    this.disabledCancel = false;
    this.disabledDownload = true;
    this.disabledDelete = true;
    this.disabledRestore = true;
  }

  /**
   * Method called by NgxDatatable on selection/deselection
   * @param {any} selected selected/unselected object
   */
  onSelect ({selected}) {
    if (isNullOrUndefined(selected) || selected.length === 0) {
      this.disableAllButtons();
      return;
    }

    this.disabledDownload = !(this.selected[0] != null && this.selected.length === 1);
    this.disabledDelete = this.selected.findIndex(sel => sel.id === this.actualId) !== -1;
    this.disabledRestore = !(this.selected[0] != null && this.selected.length === 1 && this.selected[0].id !== this.actualId);
  }

  /**
   * Method used when button save is clicked
   */
  saveButton (withDownloadCSV: boolean) {
    this.dialog.open(SaveDialogComponent).afterClosed().subscribe(result => {
      if (result) {
        this.http.delete(PModeArchiveComponent.PMODE_URL, {params: {ids: JSON.stringify(this.deleteList)}}).subscribe(() => {
            this.alertService.success('The operation \'update pmodes\' completed successfully.', false);

            this.disableAllButtons();
            this.selected = [];
            this.deleteList = [];

            if (withDownloadCSV) {
              DownloadService.downloadNative(PModeArchiveComponent.PMODE_CSV_URL);
            }
          },
          () => {
            this.alertService.error('The operation \'update pmodes\' not completed successfully.', false);
            this.getAllPModeEntries();
            this.disableAllButtons();
            this.selected = [];
          });
      } else {
        if (withDownloadCSV) {
          DownloadService.downloadNative(PModeArchiveComponent.PMODE_CSV_URL);
        }
      }
    });
  }

  /**
   * Method used when Cancel button is clicked
   */
  cancelButton () {
    this.dialog.open(CancelDialogComponent).afterClosed().subscribe(result => {
      if (result) {
        this.deleteList = [];
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
    this.disabledRestore = true;
    this.selected = [];
  }

  /**
   * Method called when Download button is clicked
   * @param row The selected row
   */
  downloadArchive (rowIndex) {
    this.download(this.tableRows[rowIndex]);
  }

  /**
   * Method called when Action Delete icon is clicked
   * @param row Row where Delete icon is located
   */
  deleteArchiveAction (row) {
    // workaround to delete one entry from the array
    // since "this.rows.splice(row, 1);" doesn't work...
    let array = this.tableRows.slice();
    this.deleteList.push(array[row].id);
    array.splice(row, 1);
    array = array.concat(this.allPModes[this.offset * this.rowLimiter.pageSize + this.rowLimiter.pageSize]);
    this.allPModes.splice(this.offset * this.rowLimiter.pageSize + row, 1);
    this.tableRows = array.slice();
    this.count--;

    if (this.offset > 0 && this.isPageEmpty()) {
      this.page(this.offset - 1, this.rowLimiter.pageSize);
    }

    setTimeout(() => {
      this.selected = [];
      this.enableSaveAndCancelButtons();
    }, 100);
  }

  /**
   * Method called when Delete button is clicked
   * All the selected rows will be deleted
   */
  deleteArchive () {
    for (let i = this.selected.length - 1; i >= 0; i--) {
      let array = this.tableRows.slice();
      array.splice(this.selected[i].$$index, 1);
      array = array.concat(this.allPModes[this.offset * this.rowLimiter.pageSize + this.rowLimiter.pageSize]);
      this.allPModes.splice(this.offset * this.rowLimiter.pageSize + this.selected[i].$$index, 1);
      this.tableRows = array.slice();
      this.deleteList.push(this.selected[i].id);
      this.count--;
    }

    if (this.offset > 0 && this.isPageEmpty()) {
      this.page(this.offset - 1, this.rowLimiter.pageSize);
    }

    this.enableSaveAndCancelButtons();
    this.selected = [];
  }

  /**
   * Method return true if all elements this.tableRows
   * are null or undefined or if is empty.
   *
   */
  isPageEmpty (): boolean {
    if (this.tableRows || this.tableRows.length) {
      for (let i = 0; i < this.tableRows.length; i++) {
        if (this.tableRows[i]) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Method called when Restore button is clicked
   * Restores the PMode for the selected row
   * - Creates a similar entry like @selectedRow
   * - Sets that entry as current
   *
   * @param selectedRow Selected Row
   */
  restoreArchive (selectedRow) {
    if (!this.isDirty()) {
      this.dialog.open(RestoreDialogComponent).afterClosed().subscribe(ok => {
        if (ok) {
          this.restore(selectedRow);
        }
      });
    } else {
      this.dialog.open(ActionDirtyDialogComponent, {
        data: {
          actionTitle: 'You will now also Restore an older version of the PMode',
          actionName: 'restore',
          actionIconName: 'settings_backup_restore'
        }
      }).afterClosed().subscribe(result => {
        if (result === 'ok') {
          this.http.delete(PModeArchiveComponent.PMODE_URL, {params: {ids: JSON.stringify(this.deleteList)}}).subscribe(result => {
              this.restore(selectedRow);
            },
            error => {
              this.alertService.error('The operation \'update pmodes\' not completed successfully.', false);
              this.enableSaveAndCancelButtons();
              this.selected = [];
            });
        } else if (result === 'restore') {
          this.restore(selectedRow);
        }
      });
    }
    this.page(0, this.rowLimiter.pageSize);
  }

  private restore (selectedRow) {
    this.allPModes[this.actualRow].current = false;
    this.http.put(PModeArchiveComponent.PMODE_URL + '/restore/' + selectedRow.id, null, {headers: this.headers}).subscribe(res => {
        this.deleteList = [];
        this.disableAllButtons();
        this.selected = [];
        this.actualRow = 0;

        this.getAllPModeEntries();
      },
      error => {
        this.alertService.error('The operation \'restore pmode\' not completed successfully.');
      });
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
  download (row) {
    this.http.get(PModeArchiveComponent.PMODE_URL + '/' + row.id).subscribe(res => {
      const uploadDateStr = DateFormatService.format(new Date(row.configurationDate));
      PModeArchiveComponent.downloadFile(res.text(), uploadDateStr);
    }, err => {
      this.alertService.error(err._body);
    });
  }

  /**
   * Method that checks if CSV Button export can be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  isSaveAsCSVButtonEnabled (): boolean {
    return this.allPModes.length < AlertComponent.MAX_COUNT_CSV;
  }

  /**
   * Saves the content of the datatable into a CSV file
   */
  saveAsCSV () {
    if (this.isDirty()) {
      this.saveButton(true);
    } else {
      DownloadService.downloadNative(PModeArchiveComponent.PMODE_CSV_URL);
    }
  }

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
    return !this.disabledCancel;
  }

  /**
   * Method called every time a tab changes
   * @param value Tab Position
   */
  selectedIndexChange (value) {
    if (value === 1 && this.uploaded) { // Archive Tab
      this.getResultObservable().map((response: Response) => response.json()).map((response) => response.slice(this.offset * this.rowLimiter.pageSize, (this.offset + 1) * this.rowLimiter.pageSize))
        .subscribe((response) => {
            this.tableRows = response;
            if (this.offset === 0) {
              this.tableRows[0].current = true;
              this.tableRows[0].description = '[CURRENT]: ' + response[0].description;
            }
            this.uploaded = false;
          }, () => {
          },
          () => {
            this.allPModes[0].current = true;
            this.actualId = this.allPModes[0].id;
            this.actualRow = 0;
            this.count = this.allPModes.length;
          });
    }
  }

  onActivate (event) {
    if ('dblclick' === event.type) {
      const current = event.row;
      this.preview(current);
    }
  }

  private preview (row) {
    this.http.get(CurrentPModeComponent.PMODE_URL + '/' + row.id + '?noAudit=true ').subscribe(res => {
      const HTTP_OK = 200;
      if (res.status === HTTP_OK) {
        const content = res.text();
        this.dialog.open(PmodeViewComponent, {
          data: {metadata: row, content: content}
        }).afterClosed().subscribe(result => {
        });
      }
    }, err => {
      this.alertService.error(err._body);
    });
  }

}

