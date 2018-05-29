import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MdDialogRef, MD_DIALOG_DATA} from '@angular/material';
import {Http} from '@angular/http';
import {AlertService} from '../../alert/alert.service';

@Component({
  selector: 'app-pmode-upload',
  templateUrl: './pmode-upload.component.html',
  styleUrls: ['../pmode.component.css']
})
export class PmodeUploadComponent implements OnInit {

  private url = 'rest/pmode';

  enableSubmit = false;
  submitInProgress = false;

  description: string = '';

  useFileSelector: boolean = true;

  @ViewChild('fileInput')
  private fileInput;

  constructor(@Inject(MD_DIALOG_DATA) private data: { pModeContents: string },
              public dialogRef: MdDialogRef<PmodeUploadComponent>, private http: Http, private alertService: AlertService) {
  }

  ngOnInit() {
    this.useFileSelector = !this.data || !this.data.pModeContents;
  }

  public checkFileAndDescription() {
    this.enableSubmit = this.hasFile() && this.description.length !== 0;
  }

  private hasFile(): boolean {
    return (this.useFileSelector && this.fileInput.nativeElement.files.length !== 0)
      || (!this.useFileSelector && !!this.data.pModeContents);
  }

  private getFile() {
    if (this.useFileSelector) {
      return this.fileInput.nativeElement.files[0];
    } else {
      return new Blob([this.data.pModeContents], {type: 'text/xml'});
    }
  }

  public submit() {
    if(this.submitInProgress) return;

    this.submitInProgress = true;

    try {
      let input = new FormData();
      input.append('file', this.getFile());
      input.append('description', this.description);
      this.http.post(this.url, input).subscribe(res => {
          this.alertService.success(res.text(), false);
          this.dialogRef.close({done: true});
        }, err => {
          this.alertService.error(err._body, false);
          this.dialogRef.close({done: false});
        },
        () => {
          this.submitInProgress = false;
        }
      );
    } catch(e) {
      this.submitInProgress = false;
    }
  }

  public cancel() {
    this.dialogRef.close({done: false})
  }

}
