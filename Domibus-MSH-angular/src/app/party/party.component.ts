import {Component, OnInit} from "@angular/core";
import {PartyDetailsComponent} from "./party-details/party-details.component";
import {MdDialog, MdDialogRef} from "@angular/material";

@Component({
  selector: 'app-party',
  templateUrl: './party.component.html',
  styleUrls: ['./party.component.css']
})
export class PartyComponent implements OnInit {

  constructor(public dialog: MdDialog) {
  }

  ngOnInit() {
    let dialogRef: MdDialogRef<PartyDetailsComponent> = this.dialog.open(PartyDetailsComponent);
  }

}
