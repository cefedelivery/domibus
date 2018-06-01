import {Component, Inject, OnInit} from '@angular/core';
import {MD_DIALOG_DATA, MdDialogRef, MdDialog, MdDialogConfig} from '@angular/material';
import {ColumnPickerBase} from 'app/common/column-picker/column-picker-base';
import {IdentifierRo, PartyIdTypeRo, PartyResponseRo, ProcessInfoRo} from '../party';
import {PartyIdentifierDetailsComponent} from '../party-identifier-details/party-identifier-details.component';

@Component({
  selector: 'app-party-details',
  templateUrl: './party-details.component.html',
  styleUrls: ['./party-details.component.css']
})
export class PartyDetailsComponent implements OnInit {

  processesRows: ProcessInfoRo[] = [];
  allProcesses: string[];

  identifiersRowColumnPicker: ColumnPickerBase = new ColumnPickerBase();
  processesRowColumnPicker: ColumnPickerBase = new ColumnPickerBase();

  party: PartyResponseRo;
  identifiers: Array<IdentifierRo>;
  selectedIdentifiers = [];


  constructor (public dialogRef: MdDialogRef<PartyDetailsComponent>, @Inject(MD_DIALOG_DATA) public data: any, private dialog: MdDialog) {
    this.party = data.edit;
    this.identifiers = this.party.identifiers;
    this.allProcesses = data.allProcesses;

    const processesWithPartyAsInitiator = this.party.processesWithPartyAsInitiator.map(el => el.name);
    const processesWithPartyAsResponder = this.party.processesWithPartyAsResponder.map(el => el.name);

    for (const proc of this.allProcesses) {
      const row = new ProcessInfoRo();
      row.name = proc;
      if (processesWithPartyAsInitiator.indexOf(proc) != -1)
        row.isInitiator = true;
      if (processesWithPartyAsResponder.indexOf(proc) != -1)
        row.isResponder = true;

      this.processesRows.push(row);
    }

    this.processesRows.sort((a, b) => {
        if (!!a.isInitiator > !!b.isInitiator) return -1;
        if (!!a.isInitiator < !!b.isInitiator) return 1;
        if (!!a.isResponder > !!b.isResponder) return -1;
        if (!!a.isResponder < !!b.isResponder) return 1;
        if (a.name < b.name) return -1;
        if (a.name > b.name) return 1;
        return 0;
      }
    );
  }

  ngOnInit () {
    this.initColumns();
  }

  initColumns () {
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
      return ['Party ID', 'Party Id Type', 'Party Id value'].indexOf(col.name) != -1
    });

    // this.processesRowColumnPicker.allColumns = [
    //   {
    //     name: 'Process',
    //     prop: 'name',
    //   },
    //   {
    //     name: 'Initiator',
    //     prop: 'isInitiator',
    //   },
    //   {
    //     name: 'Responder',
    //     prop: 'isResponder',
    //   }
    // ];
    // this.processesRowColumnPicker.selectedColumns = this.processesRowColumnPicker.allColumns.filter(col => {
    //   return ['Process', 'Initiator', 'Responder'].indexOf(col.name) != -1
    // });
  }

  editIdentifier () {
    const identifierRow = this.selectedIdentifiers[0];
    const rowClone = Object.assign({}, identifierRow);

    const dialogRef: MdDialogRef<PartyIdentifierDetailsComponent> = this.dialog.open(PartyIdentifierDetailsComponent, {
      data: {
        edit: rowClone
      }
    });
    dialogRef.afterClosed().subscribe(ok => {
      const editForm = dialogRef.componentInstance;
      if (ok) {
        Object.assign(identifierRow, editForm.data.edit);
      }
    });
  }

  removeIdentifier() {
    const identifierRow = this.selectedIdentifiers[0];
    this.party.identifiers.splice(this.party.identifiers.indexOf(identifierRow), 1);
    this.selectedIdentifiers.length = 0;
  }

  addIdentifier() {
    const identifierRow = {entityId:0, partyId: "new", partyIdType: {name: "", value: ""}};
    this.party.identifiers.push(identifierRow);
  }

  ok() {
    this.dialogRef.close(true);
  }

  cancel() {
    this.dialogRef.close(false);
  }
}
