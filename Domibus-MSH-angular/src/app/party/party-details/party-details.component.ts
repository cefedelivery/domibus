import {Component, OnInit} from "@angular/core";
import {MdDialogRef} from "@angular/material";
import {ColumnPickerBase} from "app/common/column-picker/column-picker-base";

@Component({
  selector: 'app-party-details',
  templateUrl: './party-details.component.html',
  styleUrls: ['./party-details.component.css']
})
export class PartyDetailsComponent implements OnInit {

  identifiersRow = [];
  processesRow = [];
  identifiersRowColumnPicker: ColumnPickerBase = new ColumnPickerBase();
  processesRowColumnPicker: ColumnPickerBase = new ColumnPickerBase();
  identifiersRowCount = 0;
  processesRowCount = 0;

  constructor(public dialogRef: MdDialogRef<PartyDetailsComponent>) {
  }

  ngOnInit() {
    this.initColumns();
  }

  initColumns() {
    this.identifiersRowColumnPicker.allColumns = [
      {
        name: 'Party ID',
        prop: 'partyID',
        width: 20
      },
      {
        name: 'Party Id Type',
        prop: 'partyIdType',
        width: 20
      }
    ];
    this.identifiersRowColumnPicker.selectedColumns = this.identifiersRowColumnPicker.allColumns.filter(col => {
      return ['Party ID', 'Party Id Type'].indexOf(col.name) != -1
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
}
