import {Component, OnInit} from "@angular/core";
import {MdDialog} from "@angular/material";
import {RowLimiterBase} from "app/common/row-limiter/row-limiter-base";
import {ColumnPickerBase} from "app/common/column-picker/column-picker-base";
import {PartyService} from "./party.service";
import {PartyResponseRo} from "./party";
import {Observable} from "rxjs/Observable";
import {AlertService} from "../alert/alert.service";

@Component({
  selector: 'app-party',
  providers: [PartyService],
  templateUrl: './party.component.html',
  styleUrls: ['./party.component.css']
})
export class PartyComponent implements OnInit {

  name: string;
  endPoint: string;
  partyID: string;
  process: string;

  rows = [];
  rowLimiter: RowLimiterBase = new RowLimiterBase();
  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  offset: number = 0;
  count: number = 0;
  loading: boolean = false;

  constructor(public dialog: MdDialog, public partyService: PartyService, public alertService: AlertService) {
  }

  ngOnInit() {
    this.initColumns();
    this.searchAndCount();
    //let dialogRef: MdDialogRef<PartyDetailsComponent> = this.dialog.open(PartyDetailsComponent);
  }

  searchAndCount() {
    this.loading = true;
    let pageStart = this.offset * this.rowLimiter.pageSize;
    let pageSize = this.rowLimiter.pageSize;
    let partyObservable: Observable<PartyResponseRo[]> = this.partyService.listAuditLogs(
      this.name,
      this.endPoint,
      this.partyID,
      this.process,
      pageStart,
      pageSize);
    let countObservable: Observable<number> = this.partyService.countParty(
      this.name,
      this.endPoint,
      this.partyID,
      this.process);
    Observable.zip(partyObservable, countObservable).subscribe((response) => {
        console.log('zip result');
        console.log(response);
        this.loading = false;
      },
      error => {
        this.alertService.error("Could not load parties" + error);
        this.loading = false;
      }
    );
  }

  initColumns() {
    this.columnPicker.allColumns = [
      {
        name: 'Name',
        prop: 'name',
        width: 20
      },
      {
        name: 'End point',
        prop: 'endPoint',
        width: 250
      },
      {
        name: 'Party id',
        prop: 'partyIDs',
        width: 20
      },
      {
        name: 'Process',
        prop: "processes",
        width: 30
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ["Name", "End point", "Party id", 'Process'].indexOf(col.name) != -1
    })
  }

}
