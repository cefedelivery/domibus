import {Component, OnInit} from '@angular/core';
import {MdDialog, MdDialogRef} from '@angular/material';
import {RowLimiterBase} from 'app/common/row-limiter/row-limiter-base';
import {ColumnPickerBase} from 'app/common/column-picker/column-picker-base';
import {PartyService} from './party.service';
import {PartyResponseRo, ProcessRo, ProcessInfoRo} from './party';
import {Observable} from 'rxjs/Observable';
import {AlertService} from '../alert/alert.service';
import {AlertComponent} from '../alert/alert.component';
import {PartyDetailsComponent} from './party-details/party-details.component';
import {DirtyOperations} from '../common/dirty-operations';

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Component({
  selector: 'app-party',
  providers: [PartyService],
  templateUrl: './party.component.html',
  styleUrls: ['./party.component.css']
})

export class PartyComponent implements OnInit, DirtyOperations {

  name: string;
  endPoint: string;
  partyID: string;
  process: string;

  rows = [];
  selected = [];
  rowLimiter: RowLimiterBase = new RowLimiterBase();
  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  offset: number = 0;
  count: number = 0;
  loading: boolean = false;

  newParties: PartyResponseRo[] = [];
  updatedParties: PartyResponseRo[] = [];
  deletedParties: PartyResponseRo[] = [];

  allProcesses: string[];

  constructor (public dialog: MdDialog, public partyService: PartyService, public alertService: AlertService) {
  }

  ngOnInit () {
    this.initColumns();
    this.searchAndCount();
  }

  isDirty (): boolean {
    return this.newParties.length + this.updatedParties.length + this.deletedParties.length > 0;
  }

  resetDirty () {
    this.newParties.length = 0;
    this.updatedParties.length = 0;
    this.deletedParties.length = 0;
  }

  searchAndCount () {
    this.offset = 0;

    const partyObservable = this.search();

    // this.loading = true;
    // let pageStart = this.offset * this.rowLimiter.pageSize;
    // let pageSize = this.rowLimiter.pageSize;
    //
    // let partyObservable: Observable<PartyResponseRo[]> = this.partyService.listParties(
    //   this.name,
    //   this.endPoint,
    //   this.partyID,
    //   this.process,
    //   pageStart,
    //   pageSize);

    const countObservable: Observable<number> = this.partyService.countParty(
      this.name,
      this.endPoint,
      this.partyID,
      this.process);

    Observable.zip(partyObservable, countObservable).subscribe((response) => {
        this.rows = response[0];
        this.count = response[1];
        this.loading = false;
        if (this.count > AlertComponent.MAX_COUNT_CSV) {
          this.alertService.error('Maximum number of rows reached for downloading CSV');
        }
      },
      error => {
        this.alertService.error('Could not load parties' + error);
        this.loading = false;
      }
    );
  }

  search (): Observable<PartyResponseRo[]> {
    this.loading = true;
    const pageStart = this.offset * this.rowLimiter.pageSize;
    const pageSize = this.rowLimiter.pageSize;

    const res = this.partyService.listParties(
      this.name,
      this.endPoint,
      this.partyID,
      this.process,
      pageStart,
      pageSize);

    res.subscribe((response) => {
        this.rows = response;
        this.selected.length = 0;
        // debugger;

        this.initProcesses(); // TODO : temp

        this.loading = false;
        this.resetDirty();
      },
      error => {
        this.alertService.error('Could not load parties' + error);
        this.loading = false;
      }
    );
    return res;
  }

  initProcesses() {
    const all = [];
    for (const row of this.rows) {
      for (const p1 of row.processesWithPartyAsInitiator) {
        all.push(p1.name)
      }
      for (const p1 of row.processesWithPartyAsResponder) {
        all.push(p1.name)
      }
    }
    this.allProcesses = Array.from(new Set(all));
  }

  initColumns () {
    this.columnPicker.allColumns = [
      {
        name: 'Name',
        prop: 'name',
        width: 10
      },
      {
        name: 'End point',
        prop: 'endpoint',
        width: 200
      },
      {
        name: 'Party id',
        prop: 'joinedIdentifiers',
        width: 20
      },
      {
        name: 'Process',
        prop: 'joinedProcesses',
        width: 150
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Name', 'End point', 'Party id', 'Process'].indexOf(col.name) != -1
    })
  }

  changePageSize (newPageLimit: number) {
    this.offset = 0;
    this.rowLimiter.pageSize = newPageLimit;
    this.searchAndCount();
  }

  onPage (event) {
    console.log('Page Event', event);
    this.offset = event.offset;
    this.search();
  }

  isSaveAsCSVButtonEnabled () {
    return (this.count < AlertComponent.MAX_COUNT_CSV);
  }

  saveAsCSV () {
    this.partyService.saveAsCsv(this.name, this.endPoint, this.partyID, this.process);
  }

  onActivate (event) {
    if ('dblclick' === event.type) {
      this.edit(event.row);
    }
  }

  canSave () {
    return this.isDirty();
  }

  canEdit () {
    return this.selected.length === 1;
  }

  canCancel () {
    return this.isDirty();
  }

  canDelete () {
    return this.selected.length === 1;
  }

  cancel () {
    this.search();
  }

  save () {
    // TODO
    this.partyService.updateParties(this.newParties,this.updatedParties,this.deletedParties);
    this.resetDirty();
  }

  add () {
    const newParty = this.partyService.initParty();
    this.rows.push(newParty);
    this.selected.length = 0;
    this.selected.push(newParty);
    this.count++;

    this.newParties.push(newParty);
  }

  remove () {
    const deletedParty = this.selected[0];
    this.rows.splice(this.rows.indexOf(deletedParty), 1);
    this.selected.length = 0;
    this.count--;

    if (this.newParties.indexOf(deletedParty) < 0)
      this.deletedParties.push(deletedParty);
    else
      this.newParties.splice(this.newParties.indexOf(deletedParty), 1);
  }

  edit (row) {
    row = row || this.selected[0];
    const rowCopy = JSON.parse(JSON.stringify(row));
    const allProcessesCopy = JSON.parse(JSON.stringify(this.allProcesses));
    const dialogRef: MdDialogRef<PartyDetailsComponent> = this.dialog.open(PartyDetailsComponent, {
      data: {
        edit: rowCopy,
        allProcesses: allProcessesCopy
      }
    });
    dialogRef.afterClosed().subscribe(ok => {
      if (ok) {
        if (JSON.stringify(row) == JSON.stringify(rowCopy)) return; // nothing changed

        Object.assign(row, rowCopy);
        if (this.updatedParties.indexOf(row) < 0) this.updatedParties.push(row);
      }
    });
  }
}
