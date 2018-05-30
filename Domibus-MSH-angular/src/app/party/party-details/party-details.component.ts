import {Component, Inject, OnInit} from "@angular/core";
import {MD_DIALOG_DATA, MdDialogRef, MdDialog, MdDialogConfig} from "@angular/material";
import {ColumnPickerBase} from "app/common/column-picker/column-picker-base";
import {IdentifierRo, PartyResponseRo} from "../party";
import {PartyIdentifierDetailsComponent} from "../party-identifier-details/party-identifier-details.component";

@Component({
  selector: 'app-party-details',
  templateUrl: './party-details.component.html',
  styleUrls: ['./party-details.component.css']
})
export class PartyDetailsComponent implements OnInit {

  //identifiersRow = [];
  processesRows = [];
  identifiersRowColumnPicker: ColumnPickerBase = new ColumnPickerBase();
  processesRowColumnPicker: ColumnPickerBase = new ColumnPickerBase();

  party:PartyResponseRo ;
  identifiers:Array<IdentifierRo>;
  selectedIdentifiers=[];


  constructor(public dialogRef: MdDialogRef<PartyDetailsComponent>,@Inject(MD_DIALOG_DATA) public data: any, private dialog: MdDialog) {
    this.party=data.edit;
    this.identifiers=this.party.identifiers;
  }

  ngOnInit() {
    this.initColumns();
  }

  initColumns() {
    this.identifiersRowColumnPicker.allColumns = [
      {
        name: 'Party ID',
        prop: 'partyId',
        width: 100
      },
      {
        name: 'Party Id Type',
        prop: 'partyIdType.name',
        width: 150
      },
      {
        name: 'Party Id value',
        prop: 'partyIdType.value',
        width: 280
      }
    ];
    this.identifiersRowColumnPicker.selectedColumns = this.identifiersRowColumnPicker.allColumns.filter(col => {
      return ['Party ID', 'Party Id Type','Party Id value'].indexOf(col.name) != -1
    });
    this.processesRowColumnPicker.allColumns = [
      {
        name: 'Process',
        prop: 'process',
      },
      {
        name: 'Initiator',
        prop: 'initiator',
      },
      {
        name: 'Responder',
        prop: 'responder',
      }
    ];
    this.processesRowColumnPicker.selectedColumns = this.processesRowColumnPicker.allColumns.filter(col => {
      return ['Process', 'Initiator', 'Responder'].indexOf(col.name) != -1
    });


  }

  editIdentifier() {
    let identifierRow = this.selectedIdentifiers[0];
    let dialogRef: MdDialogRef<PartyIdentifierDetailsComponent> = this.dialog.open(PartyIdentifierDetailsComponent,{
      data: {
        edit: identifierRow
      }

    });
  }

}
