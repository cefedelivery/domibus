import {Component, Inject, OnInit} from "@angular/core";
import {MD_DIALOG_DATA, MdDialogRef} from "@angular/material";
import {ColumnPickerBase} from "app/common/column-picker/column-picker-base";
import {IdentifierRo, PartyResponseRo} from "../party";

@Component({
  selector: 'app-party-details',
  templateUrl: './party-details.component.html',
  styleUrls: ['./party-details.component.css']
})
export class PartyDetailsComponent implements OnInit {

  //identifiersRow = [];
  processesRow = [];
  identifiersRowColumnPicker: ColumnPickerBase = new ColumnPickerBase();
  processesRowColumnPicker: ColumnPickerBase = new ColumnPickerBase();
  identifiersRowCount = 0;
  processesRowCount = 0;
  loading;
  party:PartyResponseRo ;
  identifiers:Array<IdentifierRo> ;


  constructor(public dialogRef: MdDialogRef<PartyDetailsComponent>,@Inject(MD_DIALOG_DATA) public data: any) {
    this.party=data.edit;
    this.identifiers=this.party.identifiers;
    this.identifiersRowCount=this.identifiers.length;
  }

  ngOnInit() {
    this.initColumns();
  }

  initColumns() {
    this.identifiersRowColumnPicker.allColumns = [
      {
        name: 'Party ID',
        prop: 'partyId',
        width: 15
      },
      {
        name: 'Party Id Type',
        prop: 'partyIdType.name',
        width: 30
      },
      {
        name: 'Party Id value',
        prop: 'partyIdType.value',
        width: 150
      }
    ];
    this.identifiersRowColumnPicker.selectedColumns = this.identifiersRowColumnPicker.allColumns.filter(col => {
      return ['Party ID', 'Party Id Type','Party Id value'].indexOf(col.name) != -1
    });
    this.processesRowColumnPicker.allColumns = [
      {
        name: 'Process',
        prop: 'process',
        width: 20
      },
      {
        name: 'Initiator',
        prop: 'initiator',
        width: 20
      },
      {
        name: 'Responder',
        prop: 'responder',
        width: 20
      }
    ];
    this.processesRowColumnPicker.selectedColumns = this.processesRowColumnPicker.allColumns.filter(col => {
      return ['Process', 'Initiator', 'Responder'].indexOf(col.name) != -1
    });


  }
  onPage(){

  }
}
