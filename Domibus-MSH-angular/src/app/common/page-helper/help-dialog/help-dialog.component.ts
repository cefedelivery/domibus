import {Component, Inject} from "@angular/core";
import {MD_DIALOG_DATA, MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-help-dialog',
  templateUrl: './help-dialog.component.html',
  styleUrls: ['./help-dialog.component.css']
})
export class HelpDialogComponent {
  pageName: String;

  constructor(public dialogRef: MdDialogRef<HelpDialogComponent>,
              @Inject(MD_DIALOG_DATA) public data: any) {
    this.pageName = data.pageName;
  }

}
