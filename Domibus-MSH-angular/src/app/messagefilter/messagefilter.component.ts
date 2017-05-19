import {Component} from "@angular/core";
import {MessagefilterDialogComponent} from "./messagefilter-dialog/messagefilter-dialog.component";
import {MdDialog} from "@angular/material";
import {AlertService} from "../alert/alert.service";
import {Http, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import {MessageFilterResult} from "./messagefilterresult";
/**
 * Created by tiago on 10/04/2017.
 */

@Component({
  moduleId: module.id,
  templateUrl: 'messagefilter.component.html',
  providers: [],
  styleUrls: ['./messagefilter.component.css']
})

export class MessageFilterComponent {

  private static NEW_VALUE = '<>';

  editing = {};
  rows = [];
  selected = [];
  rollback = [];

  rowNumber = -1;

  enableCancel = false;
  enableSave = false;
  enableDelete = false;

  enableMoveUp = false;
  enableMoveDown = false;

  loading: boolean = false;

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog) {
    this.rows.push({
      "plugin": "a",
      "from": "a",
      "to": "a",
      "action": "a",
      "service": "a"
    });

    this.rollback = this.rows.slice();
  }

  ngOnInit() {
    this.loading = true;

    this.getMessageFilterEntries().subscribe((result: MessageFilterResult) => {
      console.log("messagefilter response: " + result);

      const newRows = [];
      for(let i = 0; i < 10; i++) {
        var from = '<>', to = '<>', action = '<>', service = '<>';
        if (result[i] == null) {
          continue;
        }
        for (let j = 0; j < result[i].routingCriterias.length; j++) {
          if (result[i].routingCriterias[j] != null) {
            switch (result[i].routingCriterias[j].name) {
              case 'from':
                from = result[i].routingCriterias[j].expression;
                break;
              case 'to':
                to = result[i].routingCriterias[j].expression;
                break;
              case 'action':
                action = result[i].routingCriterias[j].expression;
                break;
              case 'service':
                service = result[i].routingCriterias[j].expression;
                break;
            }
          }
        }
        newRows.push({
          'plugin': result[i]['backendName'],
          'from': from,
          'to': to,
          'action': action,
          'service': service
        })
      }


      /*const newRows = [...result.messageFilterEntries];
      let index = 0;
      for(let i = 0; i < 10; i++) {
        newRows[i] = result.messageFilterEntries[index++];
      }*/
      this.rows = newRows;
    }, (error: any) => {
      console.log("error getting the message filter: " + error);
      this.loading = false;
      this.alertService.error("Error occured: " + error);
    });
  }

  getMessageFilterEntries(): Observable<MessageFilterResult> {
    return this.http.get('rest/messagefilters').map((response: Response) =>
      response.json()
    );
  }

  updateValue(event, cell, cellValue, row) {
    this.editing[row.$$index + '-' + cell] = false;
    this.rows[row.$$index][cell] = event.target.value;

    this.enableSave = true;
    this.enableCancel = true;
  }

  buttonNew() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;
    this.rows.push({
      "plugin": MessageFilterComponent.NEW_VALUE,
      "from": MessageFilterComponent.NEW_VALUE,
      "to": MessageFilterComponent.NEW_VALUE,
      "action": MessageFilterComponent.NEW_VALUE,
      "service": MessageFilterComponent.NEW_VALUE});
  }

  buttonCancel() {
    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;

    this.enableMoveUp = this.rowNumber  > 0;
    this.enableMoveDown = this.rowNumber != this.rows.length - 1;

    this.rows = this.rollback.slice();
  }

  buttonSave() {
    this.enableCancel = false;
    this.enableSave = false;
    this.enableDelete = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    this.rollback = this.rows.slice();
  }

  saveDialog() {
    let dialogRef = this.dialog.open(MessagefilterDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      switch(result) {
        case 'Save' :
          this.enableCancel = false;
          this.enableSave = false;
          /*var item, prop;
          for(item in this.rows) {
            for (prop in this.rows[item]) {
              if (this.rows[item][prop] == MessageFilterComponent.NEW_VALUE) {
                this.rows[item][prop] = '';
              }
            }
          }*/
          this.rollback = this.rows.slice();
          break;
        case 'Cancel':
          // do nothing
      }
    });
  }

  buttonDelete() {
    this.enableCancel = true;
    this.enableSave = true;
    this.enableDelete = false;

    this.enableMoveUp = false;
    this.enableMoveDown = false;

    this.rows.splice(this.rowNumber, 1);
  }

  buttonMoveUp() {
    if(this.rowNumber < 1) {
      return;
    }
    var array = this.rows.slice();
    var move = array[this.rowNumber];
    array[this.rowNumber] = array[this.rowNumber-1];
    array[this.rowNumber-1] = move;

    this.rows = array.slice();
    this.rowNumber--;

    if(this.rowNumber == 0) {
      this.enableMoveUp = false;
    }
    this.enableMoveDown = true;
    this.enableSave = true;
    this.enableCancel = true;
  }

  buttonMoveDown() {
    if(this.rowNumber > this.rows.length - 1) {
      return;
    }

    var array = this.rows.slice();
    var move = array[this.rowNumber];
    array[this.rowNumber] = array[this.rowNumber+1];
    array[this.rowNumber+1] = move;

    this.rows = array.slice();
    this.rowNumber++;

    if(this.rowNumber == this.rows.length - 1) {
      this.enableMoveDown = false;
    }
    this.enableMoveUp = true;
    this.enableSave = true;
    this.enableCancel = true;
  }

  onSelect({ selected }) {
    console.log('Select Event', selected, this.selected);

    this.rowNumber = this.selected[0].$$index;

    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
    this.enableMoveDown = selected.length > 0 && this.rowNumber < this.rows.length - 1;
    this.enableMoveUp = selected.length > 0 && this.rowNumber > 0;
    this.enableDelete = selected.length == 1;


  }

  /*onDeselect( { selected }) {
    this.enableMoveDown = selected.length > 0;
    this.enableMoveUp = selected.length > 0;
    this.enableDelete = selected.length == 1;
  }*/

  onActivate(event) {
    console.log('Activate Event', event);
  }
}
