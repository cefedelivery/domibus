export class ColumnPickerBase {
  columnSelection: boolean;
  allColumns = [];
  selectedColumns = [];

  changeSelectedColumns(newSelectedColumns: Array<any>) {
    this.selectedColumns = newSelectedColumns
  }

}
