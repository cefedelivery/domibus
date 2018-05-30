import { Component, OnInit, Inject } from '@angular/core';
import {MD_DIALOG_DATA, MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-party-identifier-details',
  templateUrl: './party-identifier-details.component.html',
  styleUrls: ['./party-identifier-details.component.css']
})
export class PartyIdentifierDetailsComponent implements OnInit {

  partyIdentifier: any;

  constructor(public dialogRef: MdDialogRef<PartyIdentifierDetailsComponent>, @Inject(MD_DIALOG_DATA) public data: any) {
    this.partyIdentifier = data.edit;
  }

  ngOnInit() {
  }

}
