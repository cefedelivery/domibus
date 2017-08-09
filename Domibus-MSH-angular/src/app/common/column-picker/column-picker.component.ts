import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-column-picker',
  templateUrl: './column-picker.component.html',
  styleUrls: ['./column-picker.component.css']
})
export class ColumnPickerComponent implements OnInit {

  columnSelection: boolean;

  @Input()
  allColumns = [];

  @Input()
  selectedColumns = [];

  @Output()
  onSelectedColumnsChanged = new EventEmitter<Array<any>>();

  constructor() {
  }

  ngOnInit() {
  }

  toggleColumnSelection() {
    this.columnSelection = !this.columnSelection
    return false
  }

  toggle(col) {
    const isChecked = this.isChecked(col)

    if (isChecked) {
      this.selectedColumns = this.selectedColumns.filter(c => {
        return c.name !== col.name
      });
    } else {
      this.selectedColumns = [...this.selectedColumns, col]
    }

    this.onSelectedColumnsChanged.emit(this.selectedColumns);
  }

  isChecked(col) {
    return this.selectedColumns.find(c => {
      return c.name === col.name
    });
  }

  selectAllColumns() {
    this.selectedColumns = [...this.allColumns]
    this.onSelectedColumnsChanged.emit(this.selectedColumns);
  }

  selectNoColumns() {
    this.selectedColumns = []
    this.onSelectedColumnsChanged.emit(this.selectedColumns);
  }

}
