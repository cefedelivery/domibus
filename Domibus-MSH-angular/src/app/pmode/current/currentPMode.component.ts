import {Component, OnInit} from '@angular/core';
import {Http, Response} from '@angular/http';
import {AlertService} from 'app/alert/alert.service';
import {MdDialog} from '@angular/material';
import {isNullOrUndefined} from 'util';
import {PmodeUploadComponent} from '../pmode-upload/pmode-upload.component';
import * as FileSaver from 'file-saver';
import {CancelDialogComponent} from 'app/common/cancel-dialog/cancel-dialog.component';
import {DirtyOperations} from 'app/common/dirty-operations';
import {Observable} from 'rxjs/Observable';
import {DateFormatService} from 'app/customDate/dateformat.service';

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

  public pModeExists = false;
  private pModeContents = '';
  private pModeContentsDirty = false;

  current: any;

  deleteList = [];

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
    this.getCurrentEntry();
  }

  /**
   * Gets all the PMode
   * @returns {Observable<Response>}
   */
  getResultObservable (): Observable<Response> {
    return this.http.get(CurrentPModeComponent.PMODE_URL + '/list')
      .publishReplay(1).refCount();
  }

  /**
   * Gets the current PMode entry
   */
  getCurrentEntry () {
    if (!isNullOrUndefined(CurrentPModeComponent.PMODE_URL)) {
      this.pModeContentsDirty = false;
      this.http.get(CurrentPModeComponent.PMODE_URL + '/current').subscribe(res => {
        if (res && res.text()) {
          this.current = res.json();
          this.getActivePMode();
        }
      })
    }
  }

  /**
   * Get Request for the Active PMode XML
   */
  getActivePMode () {
    if (!isNullOrUndefined(CurrentPModeComponent.PMODE_URL) && this.current !== undefined) {
      this.pModeContentsDirty = false;
      this.http.get(CurrentPModeComponent.PMODE_URL + '/' + this.current.id + '?noAudit=true ').subscribe(res => {
        const HTTP_OK = 200;
        if (res.status === HTTP_OK) {
          this.pModeExists = true;
          this.pModeContents = res.text();
        }
      }, () => {
        this.pModeExists = false;
      })
    }
  }

  /**
   * Method called when Upload button is clicked
   */
  upload () {
    this.dialog.open(PmodeUploadComponent)
      .afterClosed().subscribe(() => {
      this.getCurrentEntry();
    });
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
    this.uploadPmodeContent();
  }

  private uploadPmodeContent () {
    this.dialog.open(PmodeUploadComponent, {
      data: {pModeContents: this.pModeContents}
    }).afterClosed().subscribe(result => {
      if (result && result.done) {
        this.getCurrentEntry();
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
        this.getCurrentEntry();
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
   * Method that checks if 'Upload' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canUpload (): boolean {
    return !this.pModeExists || !this.pModeContentsDirty;
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
  }
}

